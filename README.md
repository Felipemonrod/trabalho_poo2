# Dashboards Populacionais

Sistema de análise de dados populacionais de estados e municípios brasileiros desenvolvido em Java com interface gráfica (JavaFX e Swing).

## 📋 Sobre o Projeto

Este projeto é um trabalho de Programação Orientada a Objetos 2 (POO2) que permite:

- **Leitura de dados populacionais** a partir de arquivos CSV
- **Cálculo de indicadores** como densidade demográfica e crescimento populacional
- **Visualização de dados** através de interfaces gráficas (GUI Swing e Dashboard JavaFX)
- **Exibição em console** com tabelas formatadas e rankings

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/
├── App.java                 # Ponto de entrada principal
├── model/                   # Modelos de domínio
│   ├── Regiao.java         # Classe abstrata base
│   ├── Estado.java         # Representa um estado
│   ├── Municipio.java      # Representa um município
│   └── Populacao.java      # Dados populacionais por ano
├── indicadores/            # Cálculo de indicadores
│   ├── Indicador.java      # Interface para indicadores
│   ├── DensidadeDemografica.java
│   ├── CrescimentoPopulacional.java
│   └── exceptions/         # Exceções de indicadores
├── service/                # Serviços de negócio
│   ├── Entrada.java        # Leitura de arquivos CSV
│   ├── DashboardService.java
│   ├── ImportService.java
│   └── DatabaseErrorTranslator.java
├── exceptions/             # Exceções personalizadas
├── ui/                     # Interfaces gráficas
│   ├── GuiApp.java         # Interface Swing
│   ├── RegiaoTableModel.java
│   └── fx/
│       └── DashboardApp.java  # Dashboard JavaFX
├── repository/             # Camada de persistência
├── dto/                    # Objetos de transferência de dados
└── config/                 # Configurações
```

## 🚀 Pré-requisitos

- **Java JDK 17+**
- **JavaFX SDK 25.0.1** (incluído em `lib/javafx-sdk-25.0.1/`)
- **MySQL Connector/J** (incluído em `lib/mysql-connector-j-9.5.0.jar`)

## 📦 Instalação

1. Clone o repositório:
   ```bash
   git clone https://github.com/Felipemonrod/trabalho_poo2.git
   cd trabalho_poo2
   ```

2. Certifique-se de que os arquivos de dependência estão em `lib/`:
   - `javafx-sdk-25.0.1/`
   - `mysql-connector-j-9.5.0.jar`

## 🔧 Compilação

Execute o script PowerShell de compilação:

```powershell
.\scripts\compile.ps1
```

Ou compile manualmente:

```bash
javac -d bin -encoding UTF-8 \
  -cp "lib/*" \
  --module-path lib/javafx-sdk-25.0.1/lib \
  --add-modules javafx.controls,javafx.fxml \
  src/*.java src/**/*.java
```

## ▶️ Execução

### Modo CLI (Console)

```powershell
.\scripts\run.ps1 -Mode cli
```

Exibe uma tabela formatada com os dados das regiões, incluindo:
- Código IBGE
- Tipo (Estado/Município)
- Nome
- Área (km²)
- Última população registrada
- Densidade demográfica (hab/km²)
- Crescimento populacional (%)

### Modo GUI (Interface Swing)

```powershell
.\scripts\run.ps1 -Mode gui
```

Interface gráfica tradicional com tabela de dados.

### Modo Dashboard (JavaFX)

```powershell
.\scripts\run.ps1 -Mode dash
```

Dashboard moderno com visualizações gráficas.

## 📊 Dados

Os dados populacionais estão em `data/populacao.csv` com o seguinte formato:

```csv
TIPO;CODIGO;NOME;UF;ANO;HABITANTES;AREA_KM2
ESTADO;52;Goiás;GO;;;
MUNICIPIO;5200050;Abadia de Goiás;GO;2010;449936;1013.54
MUNICIPIO;5200050;Abadia de Goiás;GO;2022;528145;1013.54
```

### Estrutura do CSV

| Campo       | Descrição                          |
|-------------|------------------------------------|
| TIPO        | ESTADO ou MUNICIPIO               |
| CODIGO      | Código IBGE                       |
| NOME        | Nome da região                    |
| UF          | Sigla do estado                   |
| ANO         | Ano do censo/estimativa           |
| HABITANTES  | Número de habitantes              |
| AREA_KM2    | Área em quilômetros quadrados     |

## 🗄️ Banco de Dados (Opcional)

O arquivo `dashboards_populacionais.sql` contém o schema do banco de dados MySQL com:

- Tabela `regiao` - Regiões geográficas do Brasil
- Tabela `estado` - Estados brasileiros
- Tabela `municipio` - Municípios
- Tabela `populacao` - Dados populacionais históricos
- Views para consultas de densidade e população por estado/ano

## 📈 Indicadores Calculados

### Densidade Demográfica
```
Densidade = População / Área (km²)
```
Resultado em habitantes por km².

### Crescimento Populacional
```
Crescimento = ((Pop. Atual - Pop. Anterior) / Pop. Anterior) × 100
```
Resultado em percentual de crescimento entre os dois últimos registros.

## 🛠️ Tecnologias Utilizadas

- **Java 17+** - Linguagem principal
- **JavaFX 25** - Interface gráfica moderna (Dashboard)
- **Swing** - Interface gráfica tradicional (GUI)
- **MySQL** - Banco de dados relacional (opcional)
- **CSV** - Formato de dados de entrada

## 📁 Estrutura de Diretórios

```
trabalho_poo2/
├── src/               # Código-fonte Java
├── lib/               # Dependências (JavaFX, MySQL Connector)
├── data/              # Arquivos de dados CSV
├── scripts/           # Scripts de compilação e execução
├── bin/               # Classes compiladas (gerado)
└── dashboards_populacionais.sql  # Schema do banco de dados
```

## 👥 Autores

- [Felipe Monrod](https://github.com/Felipemonrod)
- [Fernando Alves de Souza](https://github.com/FernandoAlves049)
- [Henrique Ferreira](https://github.com/Kobayashys)
- [Victor Hugo](https://github.com/TempestOFC)