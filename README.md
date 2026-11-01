# CineTrack — Catálogo e detalhes de filmes

Aplicativo Android desenvolvido em **Kotlin**, com **Jetpack Compose**, **MVI**, **Clean Architecture**, **Hilt**, **Retrofit**, **Kotlin Serialization**, **Coroutines** e **Coil**.

O CineTrack consome a API do [The Movie Database (TMDB)](https://www.themoviedb.org/) para exibir um catálogo de filmes e consultar os detalhes de cada título.

## Funcionalidades

- **Catálogo:** consulta `GET /3/discover/movie`, selecionando aleatoriamente uma página entre **1 e 20** a cada carregamento ou atualização. Os filmes seguem a ordem retornada pela API, sem embaralhamento adicional.
- **Detalhes:** ao selecionar um filme, consulta `GET /3/movie/{movie_id}` para obter informações atualizadas.
- **Informações exibidas:** pôster, capa, título, avaliação, data de lançamento, duração, gêneros e sinopse, quando disponíveis.
- **Atualização manual:** pull-to-refresh no catálogo e nos detalhes.
- **Estados de interface:** carregamento, sucesso, erro e tentativa novamente (*Retry*).
- **Falhas durante atualização:** preservação dos dados já exibidos e apresentação de mensagem de erro.
- **Navegação:** catálogo → detalhes → voltar à lista.

**Fora do escopo:** favoritos, Room, cache local, persistência de filmes, suporte offline e paginação contínua.

## Requisitos

- Android Studio com suporte ao projeto e ao Android Gradle Plugin utilizado.
- JDK **17**.
- Android **10 (API 29)** ou superior.
- Conexão com a internet para consultar a TMDB.
- Um **API Read Access Token** da TMDB.

## Configuração e execução

1. Obtenha o **API Read Access Token** nas [configurações de API da TMDB](https://www.themoviedb.org/settings/api).
2. Na raiz do projeto, crie ou edite `local.properties` (você pode utilizar `local.properties.example` como referência).
3. Adicione a propriedade abaixo, substituindo o valor pelo seu token real:

   ```properties
   TMDB_READ_ACCESS_TOKEN=SEU_TOKEN
   ```

4. Informe apenas o token, **sem aspas nem o prefixo `Bearer`**.
5. Abra a raiz do projeto no Android Studio, sincronize o Gradle e execute o módulo `app`.

> **Segurança:** `local.properties` não deve ser versionado. O token é incorporado ao APK via `BuildConfig`, uma simplificação adotada para este desafio técnico. Isso **não impede sua extração**; aplicações em produção precisam avaliar uma solução adequada de proteção de credenciais.

## Arquitetura

O projeto utiliza **Clean Architecture + MVI** com separação lógica de responsabilidades em **um único módulo Gradle** (`app`):

| Camada | Responsabilidade |
| --- | --- |
| `presentation` | Telas em Compose, ViewModels, estados, ações, efeitos e navegação |
| `domain` | Modelos `Movie` e `MovieDetails`, contrato `MovieRepository` e Use Cases |
| `data` | Integração com a TMDB, DTOs, mapeadores, repositório e tratamento de falhas HTTP |

As funcionalidades de catálogo e detalhes ficam organizadas em `presentation/movies/list` e `presentation/movies/details`.

Fluxo principal:

```text
Compose → ViewModel (MVI) → Use Case → MovieRepository → TMDB API
```

- `StateFlow` representa o estado contínuo da tela (`Loading`, `Success` e `Failure`).
- `Channel` é utilizado para efeitos pontuais, como mensagens de erro.
- `ApiRequestExecutor` centraliza o tratamento de falhas remotas conhecidas, convertendo-as em `DataError` e preservando o cancelamento das coroutines.
- `Refresh` e `Retry` são ações da interface; o carregamento inicial é iniciado pela ViewModel.

## Testes unitários

Os testes estão em `app/src/test` e validam, de forma isolada:

- Mapeamento de DTOs para modelos de domínio.
- Sucesso, falhas e validações do repositório.
- Configuração do cliente HTTP e conversão de erros de rede, HTTP e serialização.
- Comportamento dos Use Cases.
- Transições de estado MVI, carregamento inicial, refresh, retry e preservação do conteúdo.
- Proteção contra requisições simultâneas.

Os testes utilizam **JUnit**, `kotlinx-coroutines-test` e implementações de teste (*fakes*), sem depender da API real ou de um emulador.

Execute todos os testes unitários no Windows (PowerShell):

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## Decisões técnicas

- **MVI:** torna explícitas as ações e as transições de estado da UI.
- **Clean Architecture:** mantém o domínio independente de Retrofit e Compose.
- **Use Cases:** representam as operações de listagem e de consulta de detalhes.
- **Hilt:** centraliza a injeção de dependências.
- **Retrofit + Kotlin Serialization:** executam e interpretam as respostas da TMDB.
- **Coroutines:** oferecem execução assíncrona com cancelamento estruturado.
- **Coil:** carrega imagens remotas e permite placeholders.
- **Escopo intencionalmente reduzido:** evita dependências e funcionalidades sem benefício para os requisitos do desafio.

## Créditos

Dados e imagens fornecidos por [The Movie Database (TMDB)](https://www.themoviedb.org/).

> This product uses the TMDB API but is not endorsed or certified by TMDB.

A utilização pública do aplicativo deve observar os [requisitos de atribuição da TMDB](https://developer.themoviedb.org/docs/faq), incluindo a apresentação dos créditos e do logo aprovado na interface quando aplicável. A menção neste README não substitui os créditos exigidos no aplicativo.
