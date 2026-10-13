# Supabase — contas do BioScanner

Login é opcional. A conta fica no Supabase Auth. Os registros (observações e fotos) continuam
só no aparelho, no Room.

- `migrations/20261013000000_profiles.sql`: tabela `profiles` com RLS, e um trigger que cria o
  perfil no cadastro.
- `functions/delete-account/`: exclui a conta de quem chama. O perfil é apagado em cascata.

## Aplicar

**Pelo painel (sem CLI):**
1. Abra o SQL Editor e rode o conteúdo da migration.
2. Em Edge Functions, crie uma função `delete-account` e cole o `index.ts`. Mantenha
   "Verify JWT" ligado.

**Pela CLI:**
`supabase link --project-ref <ref>`, depois `supabase db push` e
`supabase functions deploy delete-account`.

## Configurar o Auth (painel → Authentication)

- **Email:** deixe "Confirm email" ligado e ponha **8** em "Minimum password length" (mesmo
  valor de `MIN_PASSWORD_LENGTH` no app).
- **Templates de email** (Authentication → Emails): o app confirma o cadastro e recupera a senha
  com um **código digitado no app**, não com link. Assim não precisamos de deep link nem de
  página web. Nos templates "Confirm signup" e "Reset Password", troque o link por
  `{{ .Token }}`, por exemplo: `<p>Seu código do BioScanner: <strong>{{ .Token }}</strong></p>`.
- **Google:** ative o provedor e informe o *Client ID* do OAuth Client do tipo **Web** do Google
  Cloud. No Android também é preciso um OAuth Client do tipo **Android** com o SHA-1 do
  certificado; ele não vai no Supabase, só precisa existir no mesmo projeto do Google Cloud.
- O app usa só a URL do projeto e a **anon key**, lidas do `local.properties` (que fica fora do
  git):
  ```properties
  supabase.url=https://<ref>.supabase.co
  supabase.anonKey=<anon key>
  # Client ID do OAuth Client tipo Web (o mesmo do provedor Google acima). Sem ele, o botão
  # "Continuar com Google" não aparece.
  google.webClientId=<id>.apps.googleusercontent.com
  ```
  Sem essas chaves o app funciona normalmente, mas as ações de conta falham com `NOT_CONFIGURED`.
  A `service_role` nunca vai para o app.

## Testar a exclusão

```sh
curl -X POST "$SUPABASE_URL/functions/v1/delete-account" \
  -H "Authorization: Bearer <access_token do usuário>"
# → {"deleted":true}; o usuário some de auth.users e de public.profiles
```
