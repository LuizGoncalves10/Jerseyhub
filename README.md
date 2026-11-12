# Jerseyhub

O Jerseyhub é um sistema de gestão desktop para o controle de catálogo, estoque, clientes, funcionários e pedidos de uma loja de camisas esportivas. Desenvolvido em Java com interface gráfica, o projeto integra de forma direta um banco de dados MySQL, utilizando exclusivamente comandos SQL nativos, e aplica conceitos de estatística em um dashboard com dados reais do banco.

## Funcionalidades Principais

O sistema é dividido em sete telas, acessadas por um menu lateral:

* **Loja:** Catálogo interativo em que o usuário escolhe uma seleção, ou o país, a liga e o time de um clube, e visualiza as camisas disponíveis.
* **Clientes:** CRUD completo de clientes, usando o CPF como chave de identificação. A tabela mostra a Categoria de cada cliente, calculada no banco pela função fn_classificar_cliente, que classifica em Sem compras, Bronze, Prata ou Ouro de acordo com o total gasto.
* **Camisas:** CRUD completo do catálogo, com versão, tamanho, preço, ano, equipe e estoque. A tabela mostra o Total_Vendido de cada camisa, calculado pela função fn_total_vendido_camisa. Três botões executam rotinas do banco:
  * **Reajustar:** chama o procedimento sp_reajustar_precos_equipe, que aplica um reajuste percentual nos preços de todas as camisas de uma equipe.
  * **Remessa:** chama o procedimento sp_distribuir_remessa, que usa um cursor para distribuir uma remessa de peças entre as camisas com estoque abaixo do mínimo, das mais vendidas para as menos vendidas.
  * **Histórico:** mostra a tabela Log_Camisa, preenchida automaticamente por um trigger a cada mudança de preço ou de estoque.
* **Funcionários:** CRUD completo de funcionários, com cargo e supervisor direto.
* **Pedidos:** CRUD completo de pedidos com seus itens. Os triggers do banco atualizam o estoque a cada item inserido ou removido.
* **Consultas Avançadas:** Execução de 13 consultas e 2 views do banco, cada uma com uma descrição do que ela faz, e um campo para filtrar o resultado exibido na tabela.
* **Estatísticas:** Dashboard com indicadores, filtros e gráficos gerados a partir dos dados do banco.

## Banco de Dados

O banco se chama jerseyhub e possui 12 tabelas: Equipe, Clube, Selecao, Camisa, Camisa_Cor, Cliente, Funcionario, Supervisao, Pedido, Item_Pedido, Personalizacao e Log_Camisa.

### Etapa 3 e Etapa 4: Consultas, Views e Índices

* **Consultas:** 13 consultas com JOINs, agrupamentos e subconsultas, incluindo um anti join, um full outer join feito com UNION e subconsultas com IN, NOT IN e MAX.
* **Views:** vw_faturamento_por_equipe, com o faturamento e as camisas vendidas por equipe, e vw_desempenho_vendedores, com os pedidos, os clientes atendidos e o valor vendido por funcionário.
* **Índices:** idx_cliente_nome e idx_funcionario_nome.

### Etapa 5: Funções, Procedimentos e Triggers

* **Funções:** fn_classificar_cliente, que usa estrutura condicional, e fn_total_vendido_camisa.
* **Procedimentos:** sp_reajustar_precos_equipe, procedimento de atualização de dados, e sp_distribuir_remessa, que usa cursor porque cada decisão depende do saldo que sobrou da anterior.
* **Triggers:** trg_log_alteracao_camisa, que grava o histórico na tabela Log_Camisa, e trg_baixa_estoque_item, trg_devolve_estoque_item e trg_devolve_estoque_pedido, que controlam o estoque.

Os scripts de cada entrega estão na pasta documentos_de_entregas.

## Dashboard de Estatísticas

O dashboard lê os dados do banco em tempo real e permite filtrar por período, com data inicial e final, e por tipo de equipe, que pode ser todas, somente clubes ou somente seleções.

* **Indicadores:** total de clientes, modelos cadastrados, peças em estoque, pedidos no período, faturamento no período e ticket médio.
* **Medidas de Centralização e Dispersão dos Preços:** média, mediana, moda, variância, desvio padrão e o tamanho mais frequente.
* **Intervalo de Confiança:** margem de erro com 95% de confiança para a média de peças em estoque por modelo.
* **Gráficos:**
  * Boxplot dos preços, com mínimo, Q1, mediana, Q3 e máximo.
  * Histograma de preços, com a quantidade de camisas por faixa.
  * Pizza do faturamento por versão, Torcedor e Jogador.
  * Linha do faturamento por semana.
  * Barras do faturamento por equipe, vindas da view vw_faturamento_por_equipe.
  * Barras do desempenho dos vendedores, vindas da view vw_desempenho_vendedores.
  * Barras das peças vendidas por tamanho.

## Tecnologias Utilizadas

* **Java:** Linguagem principal, usada na lógica da aplicação, na manipulação dos dados e nos cálculos estatísticos.
* **Java Swing e FlatLaf:** Construção da interface gráfica e do tema visual.
* **JDBC e MySQL Connector/J:** Conexão entre a aplicação e o banco de dados MySQL.
* **SQL Puro:** Todas as operações de inserção, atualização, exclusão e consulta são instruções SQL enviadas diretamente ao banco, sem frameworks ORM. As funções, os procedimentos e os triggers são chamados pelo código Java com CALL e SELECT.

## Como Executar

1. Instale o MySQL e abra os scripts na ordem a seguir, executando cada um por completo:
   * criacao_tabelas.sql, que cria o banco jerseyhub, as tabelas, as views, os índices, as funções e os procedimentos.
   * insercao_dados.sql, que insere os dados de exemplo e cria os triggers no final do arquivo.
   * insercao_loja_clubes.sql e insercao_loja_selecoes.sql, que preenchem o catálogo da tela Loja.
2. Configure o usuário e a senha do MySQL no arquivo banco/ConexaoBanco.java.
3. Compile o projeto incluindo os dois arquivos jar da raiz no classpath, o FlatLaf e o MySQL Connector/J.
4. Execute a classe Dashboard.

## Estrutura do Projeto

* **Dashboard.java:** Janela principal e menu lateral.
* **telas:** Um painel para cada tela do sistema, mais a classe UIUtils, com os componentes visuais compartilhados.
* **banco:** Classe de conexão com o banco de dados.
* **documentos_de_entregas:** Scripts SQL entregues em cada etapa.
* **criacao_tabelas.sql, insercao_dados.sql, insercao_loja_clubes.sql e insercao_loja_selecoes.sql:** Scripts de criação e carga do banco.

## Equipe

O desenvolvimento deste projeto foi realizado pelos seguintes membros:

* Eros Amancio Nascimento: ean@cesar.school
* Lucas Coutinho de Almeida Bayma: lcab@cesar.school
* Luiz Felipe de Siqueira Gonçalves: lfgs@cesar.school
* Mateus Lins Farias: mlf2@cesar.school
* Pedro de Sá e Benevides de Lima Moreira: psblm@cesar.school