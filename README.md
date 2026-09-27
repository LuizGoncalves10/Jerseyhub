# Jerseyhub

O Jerseyhub é um sistema de gestão desktop para o controle de catálogo, estoque e clientes de uma loja de camisas desportivas. Desenvolvido em Java com interface gráfica, o projeto integra de forma direta um banco de dados relacional, utilizando exclusivamente comandos SQL nativos, e aplica conceitos avançados de estatística para análise de dados do negócio.

## Funcionalidades Principais

O sistema é dividido em quatro abas principais através de um painel de navegação:

* **Gestão de Clientes:** Permite realizar o CRUD (Criar, Ler, Atualizar e Eliminar) de clientes de forma intuitiva, utilizando o CPF como chave de identificação.
* **Gestão de Camisas:** Controle completo do catálogo, permitindo inserir, editar e remover modelos de camisas, registando informações como versão (Torcedor/Jogador), tamanho, preço, ano, equipe e quantidade em estoque.
* **Consultas Avançadas:** Módulo para execução de consultas complexas no banco de dados, incluindo junções (JOINs) e subconsultas. Inclui visualizações como:
* Maior Receita (agrupada por equipe).
* Camisas com preço acima da média.
* Relatório geral de pedidos.
* Situação detalhada do estoque.


* **Dashboard de Estatísticas:** Um painel analítico que processa os dados do banco em tempo real para gerar métricas comerciais e matemáticas.

## Estatísticas Implementadas

Para atender aos requisitos analíticos e de análise de dados, o sistema extrai informações da base de dados e realiza cálculos matemáticos no próprio código Java, apresentando os seguintes resultados:

* **Métricas Gerais:** Contagem total de clientes, modelos de camisas e soma do volume total de peças em estoque.
* **Medidas de Centralização e Dispersão (Preços):** Cálculo da Média de preço, Moda (valor mais comum), Moda qualitativa (Tamanho mais frequente) e Desvio Padrão.
* **Medidas de Posição (Estrutura Boxplot):** Extração dos cinco pontos fundamentais para análise da distribuição dos preços das camisas:
* Valor Mínimo
* 1º Quartil (Q1)
* Mediana (Q2)
* 3º Quartil (Q3)
* Valor Máximo


* **Intervalo de Confiança:** Aplicação da fórmula de margem de erro com 95% de confiança para determinar a média populacional estimada de peças em estoque por modelo, ajudando na previsão de necessidades da loja.

## Tecnologias Utilizadas

* **Java:** Linguagem principal utilizada para a lógica de negócio, manipulação de dados e cálculos estatísticos.
* **Java Swing:** Biblioteca utilizada para a construção de toda a Interface Gráfica do Utilizador (GUI).
* **JDBC (Java Database Connectivity):** API utilizada para estabelecer a conexão entre a aplicação e o banco de dados.
* **SQL Puro:** Todas as operações de persistência e extração de dados foram feitas através de instruções SQL diretas, sem a utilização de frameworks ORM, garantindo o controle total sobre as consultas.

## Equipa

O desenvolvimento deste projeto foi realizado pelos seguintes membros:

* Eros Amancio Nascimento - ean@cesar.school
* Lucas Coutinho de Almeida Bayma - lcab@cesar.school
* Luiz Felipe de Siqueira Gonçalves - lfgs@cesar.school
* Mateus Lins Farias - mlf2@cesar.school
* Pedro de Sá e Benevides de Lima Moreira - psblm@cesar.school