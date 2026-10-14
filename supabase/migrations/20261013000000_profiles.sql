-- Perfil público do usuário. A conta em si (email, senha, identidades Google) fica em auth.users,
-- gerenciada pelo Supabase Auth; aqui guardamos só o que o app exibe.
--
-- Exclusão de conta: a Edge Function delete-account apaga a linha em auth.users e o
-- ON DELETE CASCADE leva o perfil junto. Qualquer tabela futura com dados do usuário deve
-- referenciar auth.users(id) com ON DELETE CASCADE, senão a exclusão falha ou deixa resto.

create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  display_name text not null check (char_length(display_name) between 1 and 100),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

-- O Supabase concede tudo em public para anon/authenticated por padrão; restringimos ao mínimo.
-- Sem INSERT (o trigger abaixo cria o perfil) e sem DELETE (só via exclusão da conta).
revoke all on table public.profiles from anon, authenticated;
grant select on table public.profiles to authenticated;
grant update (display_name) on table public.profiles to authenticated;

create policy "profiles_select_own" on public.profiles
  for select to authenticated
  using ((select auth.uid()) = id);

create policy "profiles_update_own" on public.profiles
  for update to authenticated
  using ((select auth.uid()) = id)
  with check ((select auth.uid()) = id);

create function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

-- Cria o perfil quando a conta nasce. O nome vem de:
--   cadastro manual → options.data.display_name enviado pelo app no signUp;
--   Google          → full_name/name que o Supabase copia do token do Google.
-- security definer porque roda no contexto do Auth, sem permissão em public.profiles.
create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, display_name)
  values (
    new.id,
    left(
      coalesce(
        nullif(trim(new.raw_user_meta_data ->> 'display_name'), ''),
        nullif(trim(new.raw_user_meta_data ->> 'full_name'), ''),
        nullif(trim(new.raw_user_meta_data ->> 'name'), ''),
        nullif(split_part(new.email, '@', 1), ''),
        'Usuário'
      ),
      100
    )
  );
  return new;
end;
$$;

revoke execute on function public.handle_new_user() from public, anon, authenticated;
revoke execute on function public.set_updated_at() from public, anon, authenticated;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
