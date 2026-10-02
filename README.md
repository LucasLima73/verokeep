# Verokeep

CLI open source em Java para migrar seu ambiente Linux entre distros diferentes: captura pacotes instalados manualmente, dotfiles, Flatpaks e repositórios numa distro, e recria tudo em outra — traduzindo nomes de pacotes entre `apt`, `dnf`, `pacman` e `zypper`.

> Status: MVP em construção. Etapa atual: detector de distro + collector de pacotes `apt` + comandos `export`/`restore` (dry-run).

## Por que

Quem testa distros com frequência precisa reconfigurar tudo do zero a cada formatação. Ferramentas como Ansible, chezmoi, Nix/home-manager, Timeshift e restic resolvem partes do problema, mas são pesadas, têm curva de aprendizado alta ou não traduzem pacotes entre distros. O Verokeep foca em: *troquei de distro, quero meu ambiente de volta em poucos minutos*.

## Uso

```bash
verokeep export -o meu-ambiente.yaml           # na distro antiga
verokeep restore meu-ambiente.yaml --dry-run    # na distro nova, só mostra o plano
verokeep restore meu-ambiente.yaml              # aplica
verokeep restore meu-ambiente.yaml --generate-script   # gera script para revisão
```

## Build

Requer Java 21 (há um `.mise.toml` fixando a versão, se você usa [mise](https://mise.jdx.dev/)).

```bash
./gradlew build
./gradlew run --args="export -o ambiente.yaml"
```

## Segurança

- Nunca exporta por padrão `~/.ssh`, `~/.gnupg`, tokens ou credenciais.
- Nunca executa `sudo` de forma escondida.
- `--dry-run` é sempre possível antes de qualquer alteração.

## Projetos relacionados

Nenhuma ferramenta encontrada resolve exatamente este problema (export/restore com tradução de nomes de pacotes entre distros), mas várias resolvem partes dele:

- [Aptik](https://github.com/teejee2008/aptik) e [apt-clone](https://github.com/mvo5/apt-clone) — backup/restore de pacotes, mas apenas dentro da mesma distro, sem tradução de nomes.
- [chezmoi](https://github.com/twpayne/chezmoi) e [yadm](https://github.com/TheLocehiliosan/yadm) — gerenciamento de dotfiles multi-máquina; referência para o futuro collector de dotfiles do Verokeep.
- [Nix / home-manager](https://github.com/nix-community/home-manager) — gestão declarativa completa do ambiente do usuário; é o "ideal" mais pesado que o Verokeep tenta evitar.
- [Timeshift](https://github.com/teejee2008/timeshift) — snapshots/rollback na mesma máquina, não migração entre distros.
- [winget export/import](https://learn.microsoft.com/windows/package-manager/winget/export) (Windows) — referência de UX para o par `export`/`restore`.
- [Repology](https://repology.org) — catálogo cross-distro de pacotes; fonte planejada como fallback do `package-map.yaml` (API com rate limit de 1 req/s, ou dumps completos em dumps.repology.org para uso offline).

## Licença

A definir.
