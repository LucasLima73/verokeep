# Projeto: migrador de ambiente entre distros Linux (nome provisório: "moult")

## Objetivo
CLI open source em Java que captura o ambiente do usuário numa distro Linux (pacotes instalados de propósito, dotfiles, Flatpaks, repositórios) e o recria em outra distro, traduzindo nomes de pacotes entre gerenciadores (apt, dnf, pacman, zypper).

Dor que resolve: quem testa distros com frequência precisa reconfigurar tudo do zero a cada formatação. Alternativas existentes (Ansible, chezmoi, Nix, Timeshift, restic) são pesadas, têm curva grande ou não traduzem pacotes entre distros. O foco aqui é: "troquei de distro, quero meu ambiente de volta em 5 minutos".

O projeto será publicado no GitHub e divulgado no LinkedIn, então README, demo (GIF) e qualidade de código importam tanto quanto as funcionalidades.

## Uso esperado
```bash
mytool export -o meu-ambiente.yaml           # na distro antiga
mytool restore meu-ambiente.yaml --dry-run   # na distro nova, só mostra o plano
mytool restore meu-ambiente.yaml             # aplica
mytool restore meu-ambiente.yaml --generate-script   # gera script para revisão
```

## Stack
- Java 21, Gradle
- Picocli (CLI)
- Jackson YAML (perfil)
- JUnit 5 + Testcontainers (containers Docker de Ubuntu, Fedora e Arch para testes reais)
- GraalVM native-image (binário único, sem exigir JVM; atenção à configuração de reflexão do Jackson)
- GitHub Actions (build e release dos binários)

## Arquitetura
Quatro blocos:
1. **Detector**: lê `/etc/os-release`, identifica distro e gerenciador de pacotes.
2. **Collectors**: `interface Collector { CollectorResult collect(); }`. Um por tipo de dado (pacotes, dotfiles, Flatpaks, repositórios, extensões do GNOME).
3. **Perfil (YAML)**: modelo neutro, independente de distro, pensado para ser versionado no Git. Usar records e `sealed interface` para os tipos de resultado.
4. **Restorers + Translator**: leem o perfil, traduzem pacotes para a distro atual e executam (ou só imprimem, em dry-run).

Exemplo de perfil:
```yaml
source: { distro: ubuntu, version: "24.04" }
packages:
  - build-essential
  - git
  - vlc
flatpaks: [org.mozilla.firefox]
dotfiles:
  - ~/.bashrc
  - ~/.config/nvim
```

## Decisões de design
- **Coletar só pacotes instalados manualmente**, nunca o sistema inteiro:
  - apt: `apt-mark showmanual`
  - pacman: `pacman -Qqe`
  - dnf: `dnf repoquery --userinstalled`
- **Tradução de pacotes** via `package-map.yaml` no repositório, com nome canônico por pacote (fácil de receber contribuições por PR):
  ```yaml
  build-tools:
    apt: build-essential
    pacman: base-devel
    dnf: "@development-tools"
  ```
  Fase futura: fallback consultando a API do Repology.
- **Execução de comandos**: wrapper em volta de `ProcessBuilder`, com modo dry-run que apenas imprime o comando.
- **Módulo de partição/backup** (rsync/restic/parted) é opcional e fica por último. Não reimplementar nada em Java: apenas orquestrar ferramentas existentes. Mexer em partição é arriscado, e o projeto já tem valor sem isso.

## Regras de segurança (inegociáveis)
- Nunca exportar por padrão: `~/.ssh`, `~/.gnupg`, tokens, senhas, credenciais. Manter lista de exclusão e avisar o usuário.
- Nunca rodar `sudo` de forma escondida. Sempre pedir confirmação ou oferecer `--generate-script` para revisão.
- Dry-run deve ser sempre possível e fácil de usar.
- Qualquer operação destrutiva exige confirmação explícita.

## Roadmap do MVP
1. Detector de distro + collector de pacotes (apt e pacman) → `export` gerando YAML
2. `restore` com dry-run (mesma distro primeiro)
3. Collector de dotfiles com lista de exclusão
4. Translator com `package-map.yaml` (apt ↔ pacman ↔ dnf)
5. Binário nativo, CI/CD, README com GIF, release v0.1
6. Módulo opcional de partição/backup

## Convenções
- Código e identificadores em inglês; README com versões em inglês (principal) e português.
- Commits pequenos e descritivos (Conventional Commits).
- Testes para cada collector e para o translator; testes de integração com Testcontainers.
- Evitar dependências desnecessárias (o binário nativo precisa continuar pequeno e compatível com GraalVM).

## Primeira tarefa
Criar o esqueleto do projeto (Gradle com Java 21, estrutura de pacotes, Picocli com os comandos `export` e `restore`), implementar o detector de distro e o primeiro collector (apt), e deixar um teste rodando. Seguir a etapa 1 do roadmap. Antes de codar, propor a estrutura de pacotes para eu aprovar.
