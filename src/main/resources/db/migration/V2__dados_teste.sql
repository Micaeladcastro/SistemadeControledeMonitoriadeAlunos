-- Dados de teste do cenario. Alguns itens (UCB, PUC-SP, CEDU, CHUM, CCSA, CCNS, CTCH...)
-- ficaram de fora de proposito, para serem cadastrados ao vivo na apresentacao.
INSERT INTO ies (sigla, nome, email, telefone, reitor, status) VALUES
 ('UCSAL',   'Universidade Catolica do Salvador',                 'contato@ucsal.br',   '(71) 3203-8800', 'Reitor UCSAL',   'ATIVO'),
 ('PUC-Rio', 'Pontificia Universidade Catolica do Rio de Janeiro','contato@puc-rio.br', '(21) 3527-1001', 'Reitor PUC-Rio', 'ATIVO');

INSERT INTO pro_reitoria (ies_id, nome, nomeado, data_nomeacao) VALUES
 ((SELECT id FROM ies WHERE sigla='UCSAL'), 'Pro-Reitoria de Graduacao', 'Pro-Reitor(a) de Graduacao UCSAL', '2024-01-15'),
 ((SELECT id FROM ies WHERE sigla='UCSAL'), 'Pro-Reitoria de Pesquisa e Pos-Graduacao', 'Pro-Reitor(a) de Pesquisa UCSAL', '2024-01-15');

INSERT INTO centro_academico (sigla, nome, email, telefone, coordenador, ies_id, status) VALUES
 ('CCET', 'Escola de Engenharias e Ciencias Tecnologicas', 'ccet@ucsal.br',     '(71) 3203-8801', 'Coordenador(a) CCET', (SELECT id FROM ies WHERE sigla='UCSAL'),   'ATIVO'),
 ('CTC',  'Centro Tecnico Cientifico',                     'ctc@puc-rio.br',    '(21) 3527-1100', 'Coordenador(a) CTC',  (SELECT id FROM ies WHERE sigla='PUC-Rio'), 'ATIVO');

INSERT INTO curso (sigla, descricao, coordenador, centro_academico_id, status) VALUES
 ('ES',   'Bacharelado em Engenharia de Software',                     'Coordenador(a) ES',   (SELECT id FROM centro_academico WHERE sigla='CCET'), 'ATIVO'),
 ('DIR',  'Direito',                                                   'Coordenador(a) DIR',  (SELECT id FROM centro_academico WHERE sigla='CCET'), 'ATIVO'),
 ('ADS',  'Tecnologico em Analise e Desenvolvimento de Sistemas',      'Coordenador(a) ADS',  (SELECT id FROM centro_academico WHERE sigla='CCET'), 'ATIVO'),
 ('CC',   'Ciencia da Computacao',                                     'Coordenador(a) CC',   (SELECT id FROM centro_academico WHERE sigla='CTC'),  'ATIVO'),
 ('ECA',  'Engenharia de Controle e Automacao',                        'Coordenador(a) ECA',  (SELECT id FROM centro_academico WHERE sigla='CTC'),  'ATIVO');

INSERT INTO curso_turno (curso_id, turno)
SELECT id, 'MATUTINO' FROM curso UNION ALL
SELECT id, 'NOTURNO'  FROM curso;
