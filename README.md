# Jerseyhub

O Jerseyhub é um sistema de gestão desktop para o controle de catálogo, estoque e clientes de uma loja de camisas desportivas. Desenvolvido em Java com interface gráfica, o projeto integra de forma direta um banco de dados relacional, utilizando exclusivamente comandos SQL nativos, e aplica conceitos avançados de estatística para análise de dados do negócio.

## Funcionalidades Principais

O sistema é dividido em quatro áreas, acessadas pela barra lateral:

* **Gestão de Camisas:** Controle completo do catálogo. A lista fica à esquerda (com busca por equipe ou modelo) e um formulário fixo à direita: clicar numa camisa carrega os dados para edição, "Nova camisa" limpa o formulário para cadastro e "Excluir" pede confirmação ali mesmo, sem janelas extras. Versão e tamanho são escolhidos em botões (só os valores aceitos pelos CHECKs do banco) e a equipe vem de uma lista carregada da tabela Equipe.
* **Gestão de Clientes:** CRUD de clientes no mesmo formato, com busca por nome ou CPF, máscara de CPF e CEP e mensagens de erro ao lado de cada campo (CPF repetido, cliente com pedidos que não pode ser excluído etc.).
* **Consultas Avançadas:** As 9 consultas do projeto, com junções (JOINs) e subconsultas. Ao escolher uma, a tela mostra a pergunta que ela responde, o SQL exato enviado ao banco e o resultado.
* **Estatísticas:** Boxplot dos preços, medidas de centralização e dispersão, modelos por tamanho e o intervalo de confiança do estoque, calculados na hora a partir do banco.

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
* **FlatLaf:** Tema visual da interface Swing (apenas aparência; não tem relação com o banco de dados).
* **Fonte Barlow:** Arquivos na pasta `fontes/` (licença SIL Open Font License, em `fontes/OFL.txt`). Se a pasta não for encontrada, o sistema usa a fonte padrão.
* **SQL Puro:** Todas as operações de persistência e extração de dados foram feitas através de instruções SQL diretas, sem a utilização de frameworks ORM, garantindo o controle total sobre as consultas.

## Equipa

O desenvolvimento deste projeto foi realizado pelos seguintes membros:

* Eros Amancio Nascimento - ean@cesar.school
* Lucas Coutinho de Almeida Bayma - lcab@cesar.school
* Luiz Felipe de Siqueira Gonçalves - lfgs@cesar.school
* Mateus Lins Farias - mlf2@cesar.school
* Pedro de Sá e Benevides de Lima Moreira - psblm@cesar.school