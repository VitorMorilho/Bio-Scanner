// Exclui definitivamente a conta de quem chama (exigência da Play Store e direito do titular na LGPD).
//
// Fluxo: o app chama com o access token do usuário → validamos o token → apagamos o usuário em
// auth.users com a service_role. O ON DELETE CASCADE remove profiles (e futuras tabelas do usuário).
// Os dados locais (registros e fotos no aparelho) são apagados pelo próprio app depois do sucesso.
//
// A service_role só existe aqui no servidor (variável injetada pelo Supabase); nunca vai para o app.

import { createClient } from "npm:@supabase/supabase-js@2.49.4";

const json = (body: unknown, status: number) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

Deno.serve(async (req) => {
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

  const authHeader = req.headers.get("Authorization") ?? "";
  const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : "";
  if (!token) return json({ error: "unauthorized" }, 401);

  const admin = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    { auth: { persistSession: false, autoRefreshToken: false } },
  );

  // O id vem do token validado, nunca do corpo da requisição: ninguém apaga a conta de outro.
  const { data, error: userError } = await admin.auth.getUser(token);
  if (userError || !data.user) return json({ error: "unauthorized" }, 401);

  // Exclusão definitiva (sem soft delete): o email fica livre para um novo cadastro.
  const { error: deleteError } = await admin.auth.admin.deleteUser(data.user.id);
  if (deleteError) {
    console.error("delete-account failed", data.user.id, deleteError.message);
    return json({ error: "delete_failed" }, 500);
  }

  return json({ deleted: true }, 200);
});
