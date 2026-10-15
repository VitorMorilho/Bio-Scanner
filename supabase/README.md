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

## Configurar o Auth

A configuração do Auth fica em `config.toml` e vai para o projeto com `supabase config push`.

- **Senha:** no mínimo 8 caracteres, com letras e números (`minimum_password_length` e
  `password_requirements`). O `validatePassword` do app usa as mesmas regras.
- **Confirmação de email: desligada por enquanto.** Sem SMTP próprio, o provedor padrão do
  plano grátis só envia email para membros da organização (cerca de 2 por hora) e não aceita
  templates personalizados. Enquanto isso, o cadastro já entra direto e o app esconde "Esqueci
  minha senha" (`auth.emailCodes=false`).
- **Com SMTP próprio** (Resend/Brevo com domínio verificado, ou Gmail com senha de app):
  configure `[auth.email.smtp]` com a senha em `env(...)`, ligue `enable_confirmations` e
  descomente os templates em `templates/`. Eles mostram só o código (`{{ .Token }}`), que é
  digitado no app, sem deep link nem página web. No app, use `auth.emailCodes=true`.
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
  # true só depois de ligar o SMTP e a confirmação por código (ver acima).
  auth.emailCodes=false
  ```
  Sem essas chaves o app funciona normalmente, mas as ações de conta falham com `NOT_CONFIGURED`.
  A `service_role` nunca vai para o app.

## Testar a exclusão

```sh
curl -X POST "$SUPABASE_URL/functions/v1/delete-account" \
  -H "Authorization: Bearer <access_token do usuário>"
# → {"deleted":true}; o usuário some de auth.users e de public.profiles
```
