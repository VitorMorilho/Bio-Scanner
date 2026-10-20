-- Registros do usuário na nuvem: a linha com data/local fica aqui e a foto no bucket
-- "observations", em <user_id>/<id>.jpg (o caminho é derivado do id, não é guardado).
--
-- O app gera o id (uuid) ao criar o registro, então reenviar depois de uma falha de rede não
-- duplica nada (insert com ON CONFLICT DO NOTHING). Registro não é editado, só criado e apagado:
-- por isso não há UPDATE na tabela.
--
-- Exclusão de conta: o ON DELETE CASCADE apaga as linhas; as fotos do Storage NÃO são apagadas em
-- cascata, quem apaga é a Edge Function delete-account antes de remover o usuário.

create table public.observations (
  id uuid primary key,
  user_id uuid not null default auth.uid() references auth.users (id) on delete cascade,
  -- Datas em epoch millis, como no app (Room), para não haver conversão de fuso.
  taken_at bigint not null,
  taken_at_origin text not null check (taken_at_origin in ('PHOTO', 'DEVICE')),
  latitude double precision check (latitude between -90 and 90),
  longitude double precision check (longitude between -180 and 180),
  location_origin text check (location_origin in ('PHOTO', 'DEVICE')),
  created_at bigint not null,
  uploaded_at timestamptz not null default now()
);

create index observations_user_id_idx on public.observations (user_id);

alter table public.observations enable row level security;

revoke all on table public.observations from anon, authenticated;
grant select, insert, delete on table public.observations to authenticated;

create policy "observations_select_own" on public.observations
  for select to authenticated
  using ((select auth.uid()) = user_id);

create policy "observations_insert_own" on public.observations
  for insert to authenticated
  with check ((select auth.uid()) = user_id);

create policy "observations_delete_own" on public.observations
  for delete to authenticated
  using ((select auth.uid()) = user_id);

-- Bucket privado: só JPEG (o app sempre grava JPEG reduzido a 1600 px), até 5 MB.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('observations', 'observations', false, 5242880, array['image/jpeg'])
on conflict (id) do nothing;

-- Cada usuário só mexe na própria pasta (<user_id>/...). UPDATE é necessário para o upload com
-- upsert, usado ao reenviar uma foto cujo envio anterior caiu no meio.
create policy "observation_photos_select_own" on storage.objects
  for select to authenticated
  using (bucket_id = 'observations' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "observation_photos_insert_own" on storage.objects
  for insert to authenticated
  with check (bucket_id = 'observations' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "observation_photos_update_own" on storage.objects
  for update to authenticated
  using (bucket_id = 'observations' and (storage.foldername(name))[1] = (select auth.uid())::text)
  with check (bucket_id = 'observations' and (storage.foldername(name))[1] = (select auth.uid())::text);

create policy "observation_photos_delete_own" on storage.objects
  for delete to authenticated
  using (bucket_id = 'observations' and (storage.foldername(name))[1] = (select auth.uid())::text);
