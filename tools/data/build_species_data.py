#!/usr/bin/env python3
"""
Gera os dados offline de espécies do app a partir de três fontes:

  1. tools/data/especies.csv   — a planilha curada (VOCÊ edita esta)
  2. GBIF                      — quais serpentes ocorrem em cada estado
  3. iNaturalist (via GBIF)    — uma foto com licença aberta por espécie

Saídas (não editar à mão, são sobrescritas):

  app/src/main/assets/seed/species_seed.json   seed do Room (espécies + estados)
  app/src/main/assets/regions/br_states.json   contornos dos 27 estados (IBGE)
  app/src/main/assets/images/species/<id>.webp fotos (600 px)

Uso, da raiz do repositório:

    python3 tools/data/build_species_data.py            # usa o cache do GBIF
    python3 tools/data/build_species_data.py --refresh  # consulta o GBIF de novo

Fluxo normal: editar o CSV → rodar o script (rápido, offline) → rodar o app.
O app recarrega o seed sozinho quando ele muda (ver DatabaseCallback).

O CSV:
- Separado por ";" e em UTF-8, abre direto no LibreOffice/Excel.
- O script só ACRESCENTA linhas para espécies novas vindas do GBIF; nunca
  apaga nem sobrescreve o que você escreveu.
- Célula vazia = sem informação (o app esconde a seção), exceto "peconhenta"
  e "denticao", que caem no padrão da família.
- "padrao_cor": um ou mais de verde, uniforme, lineado, vermelho, variegado,
  separados por "|" (ex.: "vermelho|variegado"). É o filtro do Glossário;
  espécie sem valor só aparece quando nenhum filtro está marcado.
- Para usar uma foto sua: coloque o arquivo em tools/data/fotos/ e escreva o
  nome dele na coluna "foto" (e o crédito em "credito_foto").

Decisões não óbvias:
- O backbone do GBIF NÃO tem o táxon "Serpentes" (as famílias de cobras ficam
  direto sob Squamata, junto dos lagartos). A busca usa a lista de famílias.
- O estado é filtrado por gadmGid (limite GADM aplicado às coordenadas), não
  pelo texto livre stateProvince, que vem sujo ("Rs", "PR", "Parana"...).
- Fotos só com licença CC0, CC BY ou CC BY-NC: redimensionar é uma adaptação,
  então BY-SA/ND ficam de fora. O crédito é obrigatório e vai para o app.
"""

import argparse
import csv
import gzip
import hashlib
import json
import shutil
import subprocess
import sys
import tempfile
import time
import unicodedata
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from datetime import date
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "tools/data"
CSV_PATH = DATA_DIR / "especies.csv"
OWN_PHOTOS_DIR = DATA_DIR / "fotos"
CACHE_PATH = DATA_DIR / ".cache/gbif.json"
PHOTO_CREDITS_PATH = DATA_DIR / ".cache/photo_credits.json"

ASSETS = ROOT / "app/src/main/assets"
SEED_PATH = ASSETS / "seed/species_seed.json"
STATES_PATH = ASSETS / "regions/br_states.json"
IMAGES_DIR = ASSETS / "images/species"

GBIF = "https://api.gbif.org/v1"
IBGE_MESH = (
    "https://servicodados.ibge.gov.br/api/v3/malhas/paises/BR"
    "?formato=application/vnd.geo%2Bjson&qualidade=minima&intrarregiao=UF"
)
IBGE_STATES = "https://servicodados.ibge.gov.br/api/v1/localidades/estados"
INATURALIST_DATASET = "50c9509d-22c7-4a22-a47d-8c48425ef4a7"
ALLOWED_PHOTO_LICENSES = ["CC0_1_0", "CC_BY_4_0", "CC_BY_NC_4_0"]
PHOTO_MAX_PX = 600

# Chaves do backbone do GBIF para as famílias de serpentes (aceitas).
SNAKE_FAMILY_KEYS = {
    "Acrochordidae": 9200, "Aniliidae": 5676, "Anomalepididae": 4287901,
    "Anomochilidae": 5128, "Atractaspididae": 5127, "Boidae": 2464897,
    "Bolyeriidae": 4287610, "Colubridae": 6172, "Cyclocoridae": 11182941,
    "Cylindrophiidae": 5664, "Elapidae": 9455, "Gerrhopilidae": 7398556,
    "Homalopsidae": 5789856, "Lamprophiidae": 5789868, "Leptotyphlopidae": 6931,
    "Loxocemidae": 3533, "Pareidae": 10543166, "Prosymnidae": 10765577,
    "Psammophiidae": 5789716, "Pseudaspididae": 11209474,
    "Pseudoxyrhophiidae": 10874056, "Pythonidae": 4287591,
    "Tropidophiidae": 5020, "Typhlopidae": 5021, "Uropeltidae": 5022,
    "Viperidae": 5024, "Xenodermidae": 10196291, "Xenopeltidae": 9117,
    "Xenophidiidae": 7734500, "Xenotyphlopidae": 7603232,
}

# Padrões por família, usados quando a célula do CSV está vazia. "Peçonhenta"
# = família de importância médica no Brasil; casos como Philodryas
# (opistóglifa, peçonhenta) são marcados à mão no CSV.
VENOMOUS_FAMILIES = {"Viperidae", "Elapidae"}
DENTITION_BY_FAMILY = {
    "Viperidae": "solenóglifa",
    "Elapidae": "proteróglifa",
    "Boidae": "áglifa",
    "Aniliidae": "áglifa",
    "Tropidophiidae": "áglifa",
    "Typhlopidae": "áglifa",
    "Leptotyphlopidae": "áglifa",
    "Anomalepididae": "áglifa",
    # Colubridae fica sem padrão: tem áglifas e opistóglifas.
}
DENTITIONS = {"áglifa", "opistóglifa", "proteróglifa", "solenóglifa"}
# Valor no CSV → ColorPattern (shared/.../domain/model/ColorPattern.kt).
COLOR_PATTERNS = {
    "verde": "GREEN",
    "uniforme": "UNIFORM",
    "lineado": "LINED",
    "vermelho": "RED_CORAL",
    "variegado": "VARIEGATED",
}
RISK_LEVELS = {"alto": "HIGH", "médio": "MEDIUM", "medio": "MEDIUM", "baixo": "LOW"}

# Espécies com menos registros que isso no estado são descartadas: costumam
# ser erro de identificação ou de coordenada.
MIN_OCCURRENCES = 2
MAX_OTHER_NAMES = 6

# Ordem das colunas do CSV (o cabeçalho é em português por ser editado à mão).
CSV_COLUMNS = [
    "especie", "familia", "nome_popular", "peconhenta", "risco", "denticao", "padrao_cor",
    "alimentacao", "periodo_atividade", "comportamento_defesa", "descricao",
    "distribuicao", "primeiros_socorros", "foto", "credito_foto",
]


# --------------------------------------------------------------------------
# HTTP


def http_get(url, params=None, retries=5):
    if params:
        url += "?" + urllib.parse.urlencode(params, doseq=True)
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "BioScanner-data-build"})
            with urllib.request.urlopen(req, timeout=60) as resp:
                body = resp.read()
            # O IBGE responde em gzip mesmo sem Accept-Encoding.
            if body[:2] == b"\x1f\x8b":
                body = gzip.decompress(body)
            return body
        except Exception as exc:  # noqa: BLE001 — rede instável/429: tenta de novo
            if attempt == retries - 1:
                raise
            print(f"  retry {url[:80]}… ({exc})", file=sys.stderr)
            time.sleep(2 ** attempt)


def get_json(url, params=None):
    return json.loads(http_get(url, params))


def normalize(name):
    return unicodedata.normalize("NFKD", name).encode("ascii", "ignore").decode().lower()


def slug(scientific_name):
    return normalize(scientific_name).replace(" ", "_")


# --------------------------------------------------------------------------
# GBIF / IBGE (resultado vai para o cache)


def gadm_gids_by_name():
    """
    Descobre o gadmGid de cada estado pelo próprio GBIF: a numeração do GADM
    é quase alfabética, mas não exatamente (ex.: MS vem antes de MT).
    """
    facet = get_json(f"{GBIF}/occurrence/search", {
        "country": "BR", "limit": 0, "facet": "gadmLevel1Gid", "facetLimit": 100,
    })["facets"][0]["counts"]

    def name_of(gid):
        hit = get_json(f"{GBIF}/occurrence/search", {"gadmGid": gid, "limit": 1})["results"][0]
        return normalize(hit["gadm"]["level1"]["name"]), gid

    with ThreadPoolExecutor(8) as pool:
        return dict(pool.map(name_of, (c["name"] for c in facet if c["name"].startswith("BRA."))))


def fetch_states():
    gadm_gids = gadm_gids_by_name()
    by_code = {str(s["id"]): s for s in get_json(IBGE_STATES)}
    result = []
    for feature in get_json(IBGE_MESH)["features"]:
        info = by_code[feature["properties"]["codarea"]]
        geom = feature["geometry"]
        polygons = [geom["coordinates"]] if geom["type"] == "Polygon" else geom["coordinates"]
        # Todos os anéis (inclusive buracos) vão juntos; o app desenha e testa o
        # toque com a regra par-ímpar, então buracos funcionam sem tratamento extra.
        rings = [
            [[round(lon, 3), round(lat, 3)] for lon, lat in ring]
            for polygon in polygons
            for ring in polygon
        ]
        result.append({
            "uf": info["sigla"],
            "name": info["nome"],
            "gadmGid": gadm_gids[normalize(info["nome"])],
            "rings": rings,
        })
    result.sort(key=lambda s: s["name"])
    return result


def fetch_species_counts(state):
    data = get_json(f"{GBIF}/occurrence/search", {
        "taxonKey": list(SNAKE_FAMILY_KEYS.values()),
        "gadmGid": state["gadmGid"],
        "occurrenceStatus": "PRESENT",
        "limit": 0,
        "facet": "speciesKey",
        "facetLimit": 1000,
    })
    facets = data.get("facets") or [{"counts": []}]
    return {c["name"]: c["count"] for c in facets[0]["counts"] if c["count"] >= MIN_OCCURRENCES}


def fetch_species_info(key):
    sp = get_json(f"{GBIF}/species/{key}")
    names = get_json(f"{GBIF}/species/{key}/vernacularNames", {"limit": 200})["results"]
    seen, others = set(), []
    for n in names:
        name = n["vernacularName"].strip().capitalize()
        if n.get("language") == "por" and name.lower() not in seen:
            seen.add(name.lower())
            others.append(name)
    return key, {
        "gbifKey": int(key),
        "scientificName": sp.get("canonicalName") or sp.get("scientificName"),
        "family": sp.get("family"),
        "vernacularNames": sorted(others),
    }


def load_gbif(refresh):
    if CACHE_PATH.exists() and not refresh:
        print(f"Usando cache do GBIF ({CACHE_PATH.relative_to(ROOT)}); --refresh para atualizar.")
        return json.loads(CACHE_PATH.read_text(encoding="utf-8"))

    print("Baixando contornos dos estados (IBGE)…")
    states = fetch_states()
    print("Contando serpentes por estado (GBIF)…")
    with ThreadPoolExecutor(8) as pool:
        counts = dict(zip((s["uf"] for s in states), pool.map(fetch_species_counts, states)))
    keys = sorted({k for c in counts.values() for k in c}, key=int)
    print(f"Buscando nomes de {len(keys)} espécies…")
    with ThreadPoolExecutor(8) as pool:
        info = dict(pool.map(fetch_species_info, keys))

    cache = {"generatedAt": date.today().isoformat(), "states": states, "counts": counts, "species": info}
    CACHE_PATH.parent.mkdir(parents=True, exist_ok=True)
    CACHE_PATH.write_text(json.dumps(cache, ensure_ascii=False), encoding="utf-8")
    return cache


# --------------------------------------------------------------------------
# CSV curado


def read_csv():
    if not CSV_PATH.exists():
        return []
    with CSV_PATH.open(encoding="utf-8-sig", newline="") as f:
        return [
            {col: (row.get(col) or "").strip() for col in CSV_COLUMNS}
            for row in csv.DictReader(f, delimiter=";")
        ]


def write_csv(rows):
    # utf-8-sig: o BOM faz o Excel reconhecer os acentos ao abrir o arquivo.
    with CSV_PATH.open("w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=CSV_COLUMNS, delimiter=";")
        writer.writeheader()
        writer.writerows(rows)


def sync_csv(rows, gbif_species):
    """Acrescenta linhas para espécies novas do GBIF; não altera as existentes."""
    known = {r["especie"] for r in rows}
    new = [
        {
            **{col: "" for col in CSV_COLUMNS},
            "especie": sp["scientificName"],
            "familia": sp["family"] or "",
            "denticao": DENTITION_BY_FAMILY.get(sp["family"], ""),
        }
        for sp in gbif_species.values()
        if sp["scientificName"] not in known
    ]
    if new:
        print(f"CSV: {len(new)} espécies novas acrescentadas.")
        rows = sorted(rows + new, key=lambda r: (r["familia"], r["especie"]))
        write_csv(rows)
    return rows


def parse_bool(value, default):
    v = normalize(value)
    if v in ("sim", "s", "true", "1"):
        return True
    if v in ("nao", "n", "false", "0"):
        return False
    return default


def parse_color_patterns(value):
    return [normalize(v.strip()) for v in value.split("|") if v.strip()]


def validate(row, line):
    problems = []
    for pattern in parse_color_patterns(row["padrao_cor"]):
        if pattern not in COLOR_PATTERNS:
            problems.append(f"padrao_cor '{pattern}' (use: {', '.join(COLOR_PATTERNS)}, separados por |)")
    if row["denticao"] and row["denticao"].lower() not in DENTITIONS:
        problems.append(f"denticao '{row['denticao']}' (use: {', '.join(sorted(DENTITIONS))})")
    if row["risco"] and row["risco"].lower() not in RISK_LEVELS:
        problems.append(f"risco '{row['risco']}' (use: alto, médio, baixo)")
    if row["foto"] and not (OWN_PHOTOS_DIR / row["foto"]).exists():
        problems.append(f"foto '{row['foto']}' não existe em {OWN_PHOTOS_DIR.relative_to(ROOT)}")
    if row["foto"] and not row["credito_foto"]:
        problems.append("foto própria sem credito_foto")
    return [f"linha {line} ({row['especie']}): {p}" for p in problems]


# --------------------------------------------------------------------------
# Fotos


def license_label(url):
    url = url or ""
    if "publicdomain/zero" in url:
        return "CC0"
    if "/by-nc/" in url:
        return "CC BY-NC"
    if "/by/" in url:
        return "CC BY"
    return None  # outras (BY-SA, BY-ND, todos os direitos) ficam de fora


def find_photo(gbif_key):
    """Primeira foto com licença aceita: iNaturalist no Brasil, depois qualquer fonte."""
    attempts = [
        {"datasetKey": INATURALIST_DATASET, "country": "BR"},
        {"datasetKey": INATURALIST_DATASET},
        {},
    ]
    for extra in attempts:
        results = get_json(f"{GBIF}/occurrence/search", {
            "taxonKey": gbif_key, "mediaType": "StillImage",
            "license": ALLOWED_PHOTO_LICENSES, "limit": 10, **extra,
        })["results"]
        for occ in results:
            for media in occ.get("media", []):
                label = license_label(media.get("license"))
                url = media.get("identifier")
                # Algumas fontes apontam para uma página web, não para o arquivo.
                is_image = (media.get("format") or "").startswith("image/") or \
                    (url or "").lower().split("?")[0].endswith((".jpg", ".jpeg", ".png"))
                if media.get("type") == "StillImage" and url and label and is_image:
                    author = media.get("creator") or media.get("rightsHolder") or occ.get("recordedBy") or "autor desconhecido"
                    source = "iNaturalist" if occ.get("datasetKey") == INATURALIST_DATASET else "GBIF"
                    # O "original" do iNaturalist pode ter vários MB; "large" (1024 px) basta.
                    return url.replace("/original.", "/large."), f"Foto: {author}, {label} ({source})"
    return None


def convert_photo(src, dest):
    dest.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        ["convert", str(src), "-auto-orient", "-resize", f"{PHOTO_MAX_PX}x{PHOTO_MAX_PX}>",
         "-strip", "-quality", "72", f"webp:{dest}"],
        check=True,
    )


def build_photos(species):
    """Gera assets/images/species/<id>.webp; baixa só o que ainda não existe."""
    if not shutil.which("convert"):
        raise SystemExit("ImageMagick ('convert') não encontrado: sudo apt install imagemagick")
    credits = json.loads(PHOTO_CREDITS_PATH.read_text(encoding="utf-8")) if PHOTO_CREDITS_PATH.exists() else {}

    def handle(sp):
        try:
            return handle_one(sp)
        except Exception as exc:  # noqa: BLE001 — uma foto ruim não derruba o resto
            print(f"  sem foto para {sp['scientificName']}: {exc}", file=sys.stderr)
            return sp["id"], None

    def handle_one(sp):
        dest = IMAGES_DIR / f"{sp['id']}.webp"
        own = sp["_ownPhoto"]
        if own:
            src = OWN_PHOTOS_DIR / own
            # Foto própria: reconverte só se o arquivo original mudou.
            stamp = f"own:{own}:{src.stat().st_mtime_ns}"
            if credits.get(sp["id"], {}).get("stamp") != stamp or not dest.exists():
                convert_photo(src, dest)
            return sp["id"], {"stamp": stamp, "credit": sp["_ownCredit"]}
        cached = credits.get(sp["id"])
        if dest.exists() and cached and not cached["stamp"].startswith("own:"):
            return sp["id"], cached
        if not sp["gbifKey"]:
            return sp["id"], None
        found = find_photo(sp["gbifKey"])
        if not found:
            return sp["id"], None
        url, credit = found
        data = http_get(url)
        if not (data[:3] == b"\xff\xd8\xff" or data[:8] == b"\x89PNG\r\n\x1a\n"):
            raise ValueError(f"{url} não é JPEG/PNG")
        with tempfile.NamedTemporaryFile(suffix=".jpg") as tmp:
            tmp.write(data)
            tmp.flush()
            convert_photo(tmp.name, dest)
        return sp["id"], {"stamp": url, "credit": credit}

    with ThreadPoolExecutor(6) as pool:
        results = dict(pool.map(handle, species))

    for sp in species:
        del sp["_ownPhoto"], sp["_ownCredit"]
        entry = results.get(sp["id"])
        sp["referenceImagePath"] = f"images/species/{sp['id']}.webp" if entry else None
        sp["imageCredit"] = entry["credit"] if entry else None

    # Remove fotos de espécies que saíram da lista.
    valid = {f"{sp['id']}.webp" for sp in species}
    for f in IMAGES_DIR.glob("*.webp"):
        if f.name not in valid:
            f.unlink()

    PHOTO_CREDITS_PATH.parent.mkdir(parents=True, exist_ok=True)
    PHOTO_CREDITS_PATH.write_text(
        json.dumps({k: v for k, v in results.items() if v}, ensure_ascii=False, indent=1), encoding="utf-8")
    missing = [sp["scientificName"] for sp in species if not sp["referenceImagePath"]]
    if missing:
        print(f"Sem foto com licença aberta ({len(missing)}): {', '.join(missing[:10])}{'…' if len(missing) > 10 else ''}")


# --------------------------------------------------------------------------


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--refresh", action="store_true", help="consulta o GBIF/IBGE de novo")
    parser.add_argument("--no-photos", action="store_true", help="não baixa/gera fotos")
    args = parser.parse_args()

    gbif = load_gbif(args.refresh)
    rows = sync_csv(read_csv(), gbif["species"])

    problems = [p for i, row in enumerate(rows, start=2) for p in validate(row, i)]
    if problems:
        raise SystemExit("Corrija o CSV:\n  " + "\n  ".join(problems))

    by_name = {sp["scientificName"]: sp for sp in gbif["species"].values()}
    regions_by_key = {}
    for uf, counts in gbif["counts"].items():
        for key, n in counts.items():
            regions_by_key.setdefault(key, []).append({"regionId": uf, "occurrences": n})

    species = []
    for row in rows:
        g = by_name.get(row["especie"], {})
        family = row["familia"] or g.get("family") or ""
        key = str(g["gbifKey"]) if g else None
        popular = row["nome_popular"] or None
        others = [n for n in g.get("vernacularNames", []) if not popular or n.lower() != popular.lower()]
        species.append({
            "id": slug(row["especie"]),
            "gbifKey": g.get("gbifKey"),
            "scientificName": row["especie"],
            "popularName": popular,
            "otherNames": others[:MAX_OTHER_NAMES],
            "family": family,
            "category": "snake",
            "venomous": parse_bool(row["peconhenta"], family in VENOMOUS_FAMILIES),
            "riskLevel": RISK_LEVELS.get(row["risco"].lower(), "UNKNOWN"),
            "dentition": (row["denticao"] or DENTITION_BY_FAMILY.get(family, "")).lower() or None,
            "diet": row["alimentacao"] or None,
            "activityPeriod": row["periodo_atividade"] or None,
            "defensiveBehavior": row["comportamento_defesa"] or None,
            "colorPatterns": [COLOR_PATTERNS[p] for p in dict.fromkeys(parse_color_patterns(row["padrao_cor"]))],
            "description": row["descricao"] or None,
            "geographicDistribution": row["distribuicao"] or None,
            "firstAidProcedure": row["primeiros_socorros"] or None,
            "regions": sorted(regions_by_key.get(key, []), key=lambda r: -r["occurrences"]),
            "_ownPhoto": row["foto"] or None,
            "_ownCredit": row["credito_foto"] or None,
        })

    if args.no_photos:
        # Reaproveita os créditos já gravados: sem isso o seed perderia o
        # crédito das fotos existentes (obrigatório pela licença).
        credits = json.loads(PHOTO_CREDITS_PATH.read_text(encoding="utf-8")) if PHOTO_CREDITS_PATH.exists() else {}
        for sp in species:
            sp.pop("_ownPhoto"), sp.pop("_ownCredit")
            exists = (IMAGES_DIR / f"{sp['id']}.webp").exists()
            sp["referenceImagePath"] = f"images/species/{sp['id']}.webp" if exists else None
            sp["imageCredit"] = credits.get(sp["id"], {}).get("credit") if exists else None
    else:
        print("Gerando fotos…")
        build_photos(species)

    body = json.dumps(species, ensure_ascii=False, separators=(",", ":"))
    # A versão muda sempre que qualquer dado muda: é o que faz o app recarregar o seed.
    version = hashlib.sha1(body.encode()).hexdigest()[:12]
    SEED_PATH.parent.mkdir(parents=True, exist_ok=True)
    SEED_PATH.write_text(f'{{"version":"{version}","species":{body}}}', encoding="utf-8")
    STATES_PATH.parent.mkdir(parents=True, exist_ok=True)
    STATES_PATH.write_text(
        json.dumps({
            "source": "GBIF.org e IBGE",
            "generatedAt": gbif["generatedAt"],
            "states": [{k: s[k] for k in ("uf", "name", "rings")} for s in gbif["states"]],
        }, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )

    with_photo = sum(1 for sp in species if sp["referenceImagePath"])
    curated = sum(1 for sp in species if sp["defensiveBehavior"] or sp["description"])
    print(f"OK: {len(species)} espécies ({curated} com texto curado, {with_photo} com foto), seed v{version}")


if __name__ == "__main__":
    main()
