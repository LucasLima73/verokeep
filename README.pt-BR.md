<p align="right">
  <a href="README.md">🇺🇸 Read in English</a>
</p>

<h1 align="center">Verokeep</h1>

<p align="center">
  Capture seu ambiente Linux numa distro, recrie em outra — nomes de pacotes e tudo.
</p>

<p align="center">
  <a href="https://github.com/LucasLima73/verokeep/actions/workflows/ci.yml"><img src="https://github.com/LucasLima73/verokeep/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <a href="https://github.com/LucasLima73/verokeep/actions/workflows/release.yml"><img src="https://github.com/LucasLima73/verokeep/actions/workflows/release.yml/badge.svg" alt="Release"></a>
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/status-MVP%20em%20constru%C3%A7%C3%A3o-yellow" alt="Status">
</p>

---

CLI open source, em Java, que captura o ambiente do usuário numa distro Linux — pacotes instalados
manualmente, dotfiles, Flatpaks, repositórios — e o recria em outra, traduzindo nomes de pacotes entre
`apt`, `dnf`, `pacman` e `zypper`.

> **Status: MVP em construção**
> - ✅ Detector de distro + collectors `apt`/`pacman`
> - ✅ `export`/`restore` (plano/dry-run)
> - ✅ Collector de dotfiles com exclusão de segurança
> - ✅ Translator de pacotes via `package-map.yaml`
> - ✅ Binário nativo (GraalVM) + CI + release automatizado
> - ⏳ `restore` ainda não aplica as mudanças de verdade (só mostra o plano)
> - ⏳ GIF de demonstração

## Por que

Quem testa distros com frequência precisa reconfigurar tudo do zero a cada formatação. Ferramentas como
Ansible, chezmoi, Nix/home-manager, Timeshift e restic resolvem partes do problema, mas são pesadas, têm
curva de aprendizado alta ou simplesmente não traduzem nomes de pacotes entre distros. O Verokeep foca
em um único objetivo: *troquei de distro, quero meu ambiente de volta em poucos minutos.*

## Uso

```bash
verokeep export -o meu-ambiente.yaml           # na distro antiga
verokeep restore meu-ambiente.yaml --dry-run    # na distro nova, só mostra o plano
verokeep restore meu-ambiente.yaml              # aplica
verokeep restore meu-ambiente.yaml --generate-script   # gera script para revisão

verokeep backup --source ~/Projetos --dest /mnt/backup/Projetos          # dry-run (padrão)
verokeep backup --source ~/Projetos --dest /mnt/backup/Projetos --apply  # aplica de fato (opcional, via rsync)
```

## Build

Requer Java 21 (há um `.mise.toml` fixando a versão, se você usa [mise](https://mise.jdx.dev/)).

```bash
./gradlew build
./gradlew run --args="export -o ambiente.yaml"
```

### Binário nativo (GraalVM)

Requer uma distribuição GraalVM (ex: via [mise](https://mise.jdx.dev/) ou [sdkman](https://sdkman.io/))
como JDK ativo:

```bash
./gradlew nativeCompile
./build/native/nativeCompile/verokeep export -o ambiente.yaml
```

A configuração de reflexão/recursos para o `native-image` (Jackson, `package-map.yaml`) está em
`src/main/resources/META-INF/native-image/`. Ainda não foi validada com um build nativo real — se der
erro de reflexão em algum tipo, use o
[tracing agent](https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/)
do GraalVM para regenerar/ajustar esses arquivos.

### CI/CD

- `.github/workflows/ci.yml` — roda `./gradlew build` (compila + testes) em todo push/PR para `main`.
- `.github/workflows/release.yml` — builda o binário nativo Linux via GraalVM e publica como asset da
  GitHub Release, disparado ao empurrar uma tag `v*`:

  ```bash
  git tag v0.1.0
  git push origin v0.1.0
  ```

## Segurança

- Nunca exporta por padrão `~/.ssh`, `~/.gnupg`, tokens ou credenciais.
- Nunca executa `sudo` de forma escondida.
- `--dry-run` é sempre possível antes de qualquer alteração.

## Projetos relacionados

Nenhuma ferramenta encontrada resolve exatamente este problema (export/restore com tradução de nomes de
pacotes entre distros), mas várias resolvem partes dele:

- [Aptik](https://github.com/teejee2008/aptik) e [apt-clone](https://github.com/mvo5/apt-clone) —
  backup/restore de pacotes, mas apenas dentro da mesma distro, sem tradução de nomes.
- [chezmoi](https://github.com/twpayne/chezmoi) e [yadm](https://github.com/TheLocehiliosan/yadm) —
  gerenciamento de dotfiles multi-máquina; referência para o collector de dotfiles do Verokeep.
- [Nix / home-manager](https://github.com/nix-community/home-manager) — gestão declarativa completa do
  ambiente do usuário; é o "ideal" mais pesado que o Verokeep tenta evitar.
- [Timeshift](https://github.com/teejee2008/timeshift) — snapshots/rollback na mesma máquina, não
  migração entre distros.
- [winget export/import](https://learn.microsoft.com/windows/package-manager/winget/export) (Windows) —
  referência de UX para o par `export`/`restore`.
- [Repology](https://repology.org) — catálogo cross-distro de pacotes; fonte planejada como fallback do
  `package-map.yaml` (API com rate limit de 1 req/s, ou dumps completos em dumps.repology.org para uso
  offline).

## Licença

A definir.

## Contribuindo

Issues e PRs são bem-vindos — principalmente adições em `src/main/resources/package-map.yaml` para
pacotes cujo nome muda entre distros. Commits seguem [Conventional Commits](https://www.conventionalcommits.org/).
