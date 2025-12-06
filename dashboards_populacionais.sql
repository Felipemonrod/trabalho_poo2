-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 26/11/2025 às 19:20
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `dashboards_populacionais`
--

-- --------------------------------------------------------

--
-- Estrutura para tabela `estado`
--

CREATE TABLE `estado` (
  `id` int(11) NOT NULL,
  `nome` varchar(100) NOT NULL,
  `sigla` char(2) NOT NULL,
  `area_km2` decimal(12,2) DEFAULT NULL,
  `codigo_ibge` int(11) DEFAULT NULL,
  `id_regiao` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `estado`
--

INSERT INTO `estado` (`id`, `nome`, `sigla`, `area_km2`, `codigo_ibge`, `id_regiao`) VALUES
(1, 'Goiás', 'GO', 2024.00, 52, 5),
(2, 'Minas Gerais', 'MG', 586528.29, 31, 3),
(3, 'São Paulo', 'SP', 248222.36, 35, 3),
(4, 'Paraná', 'PR', 2024.00, 41, NULL);

-- --------------------------------------------------------

--
-- Estrutura para tabela `municipio`
--

CREATE TABLE `municipio` (
  `id` int(11) NOT NULL,
  `id_estado` int(11) NOT NULL,
  `nome` varchar(120) NOT NULL,
  `area_km2` decimal(12,2) DEFAULT NULL,
  `codigo_ibge` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `municipio`
--

INSERT INTO `municipio` (`id`, `id_estado`, `nome`, `area_km2`, `codigo_ibge`) VALUES
(1, 1, 'Morrinhos', 2854.00, 5213800),
(2, 1, 'Goiânia', 739.50, 5208707),
(3, 1, 'Rio Verde', 8301.00, 5218805),
(4, 2, 'Uberlândia', 4115.00, 3170206),
(5, 3, 'Campinas', 795.00, 3509502),
(6, 1, 'Aparecida de Goiânia', 288.00, 5201405),
(7, 1, 'Anápolis', 918.00, 5201108),
(8, 2, 'Belo Horizonte', 331.00, 3106200),
(9, 2, 'Contagem', 195.00, 3118601),
(10, 3, 'São Paulo', 1521.00, 3550308),
(11, 3, 'Santos', 281.00, 3548500),
(12, 4, 'Curitiba', 435.04, 4106902);

-- --------------------------------------------------------

--
-- Estrutura para tabela `populacao`
--

CREATE TABLE `populacao` (
  `id` int(11) NOT NULL,
  `id_municipio` int(11) NOT NULL,
  `ano` int(11) NOT NULL,
  `habitantes` bigint(20) NOT NULL,
  `fonte` varchar(120) DEFAULT 'IBGE',
  `criado_em` timestamp NOT NULL DEFAULT current_timestamp(),
  `atualizado_em` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `populacao`
--

INSERT INTO `populacao` (`id`, `id_municipio`, `ano`, `habitantes`, `fonte`, `criado_em`, `atualizado_em`) VALUES
(1, 1, 2010, 41100, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(2, 1, 2020, 45200, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(3, 1, 2024, 47000, 'Estimativa', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(4, 2, 2010, 1302500, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(5, 2, 2020, 1536000, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(6, 2, 2024, 1650500, 'CSV', '2025-10-15 22:37:56', '2025-11-26 17:26:16'),
(7, 3, 2010, 176000, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(8, 3, 2020, 235000, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(9, 3, 2024, 255000, 'Estimativa', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(10, 4, 2010, 604013, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(11, 4, 2020, 713232, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(12, 4, 2024, 750000, 'Estimativa', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(13, 5, 2010, 1080116, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(14, 5, 2020, 1213792, 'IBGE', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(15, 5, 2024, 1250000, 'Estimativa', '2025-10-15 22:37:56', '2025-10-15 22:37:56'),
(16, 1, 2000, 37000, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(17, 1, 2015, 43500, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(18, 1, 2022, 46000, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(19, 2, 2000, 1093007, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(20, 2, 2015, 1430697, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(21, 2, 2022, 1437237, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(22, 3, 2000, 116552, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(23, 3, 2015, 207096, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(24, 3, 2022, 225696, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(25, 6, 2000, 336392, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(26, 6, 2010, 455657, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(27, 6, 2015, 521910, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(28, 6, 2020, 590146, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(29, 6, 2022, 527550, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(30, 6, 2024, 550000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(31, 7, 2000, 288085, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(32, 7, 2010, 334613, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(33, 7, 2015, 366491, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(34, 7, 2020, 391772, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(35, 7, 2022, 398817, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(36, 7, 2024, 415000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(37, 4, 2000, 501214, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(38, 4, 2015, 662362, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(39, 4, 2022, 713224, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(40, 8, 2000, 2238526, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(41, 8, 2010, 2375151, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(42, 8, 2015, 2502557, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(43, 8, 2020, 2521564, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(44, 8, 2022, 2315560, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(45, 8, 2024, 2400000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(46, 9, 2000, 538017, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(47, 9, 2010, 603442, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(48, 9, 2015, 648766, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(49, 9, 2020, 668949, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(50, 9, 2022, 621865, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(51, 9, 2024, 630000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(52, 5, 2000, 969910, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(53, 5, 2015, 1164098, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(54, 5, 2022, 1138309, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(55, 10, 2000, 10434252, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(56, 10, 2010, 11253503, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(57, 10, 2015, 11967825, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(58, 10, 2020, 12325232, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(59, 10, 2022, 11451245, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(60, 10, 2024, 11800000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(61, 11, 2000, 417477, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(62, 11, 2010, 419400, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(63, 11, 2015, 433966, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(64, 11, 2020, 433656, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(65, 11, 2022, 418608, 'Censo IBGE', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(66, 11, 2024, 420000, 'Estimativa', '2025-11-26 16:01:58', '2025-11-26 16:01:58'),
(67, 12, 2024, 1784191, 'CSV', '2025-11-26 17:26:16', '2025-11-26 17:26:16');

-- --------------------------------------------------------

--
-- Estrutura para tabela `regiao`
--

CREATE TABLE `regiao` (
  `id` int(11) NOT NULL,
  `nome` varchar(50) NOT NULL,
  `sigla` char(2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `regiao`
--

INSERT INTO `regiao` (`id`, `nome`, `sigla`) VALUES
(1, 'Norte', 'N'),
(2, 'Nordeste', 'NE'),
(3, 'Sudeste', 'SE'),
(4, 'Sul', 'S'),
(5, 'Centro-Oeste', 'CO');

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_densidade_municipal`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_densidade_municipal` (
`id_municipio` int(11)
,`municipio` varchar(120)
,`uf` char(2)
,`regiao` varchar(50)
,`ano` int(11)
,`habitantes` bigint(20)
,`area_km2` decimal(12,2)
,`densidade_hab_km2` decimal(24,2)
);

-- --------------------------------------------------------

--
-- Estrutura stand-in para view `vw_populacao_estado_por_ano`
-- (Veja abaixo para a visão atual)
--
CREATE TABLE `vw_populacao_estado_por_ano` (
`id_estado` int(11)
,`estado` varchar(100)
,`sigla` char(2)
,`regiao` varchar(50)
,`ano` int(11)
,`habitantes` decimal(41,0)
);

-- --------------------------------------------------------

--
-- Estrutura para view `vw_densidade_municipal`
--
DROP TABLE IF EXISTS `vw_densidade_municipal`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_densidade_municipal`  AS SELECT `m`.`id` AS `id_municipio`, `m`.`nome` AS `municipio`, `e`.`sigla` AS `uf`, `r`.`nome` AS `regiao`, `p`.`ano` AS `ano`, `p`.`habitantes` AS `habitantes`, `m`.`area_km2` AS `area_km2`, CASE WHEN `m`.`area_km2` > 0 THEN round(`p`.`habitantes` / `m`.`area_km2`,2) ELSE NULL END AS `densidade_hab_km2` FROM (((`municipio` `m` join `estado` `e` on(`e`.`id` = `m`.`id_estado`)) join `regiao` `r` on(`r`.`id` = `e`.`id_regiao`)) join `populacao` `p` on(`p`.`id_municipio` = `m`.`id`)) ORDER BY `r`.`nome` ASC, `e`.`sigla` ASC, `m`.`nome` ASC, `p`.`ano` ASC ;

-- --------------------------------------------------------

--
-- Estrutura para view `vw_populacao_estado_por_ano`
--
DROP TABLE IF EXISTS `vw_populacao_estado_por_ano`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vw_populacao_estado_por_ano`  AS SELECT `e`.`id` AS `id_estado`, `e`.`nome` AS `estado`, `e`.`sigla` AS `sigla`, `r`.`nome` AS `regiao`, `p`.`ano` AS `ano`, sum(`p`.`habitantes`) AS `habitantes` FROM (((`estado` `e` join `regiao` `r` on(`r`.`id` = `e`.`id_regiao`)) join `municipio` `m` on(`m`.`id_estado` = `e`.`id`)) join `populacao` `p` on(`p`.`id_municipio` = `m`.`id`)) GROUP BY `e`.`id`, `e`.`nome`, `e`.`sigla`, `r`.`nome`, `p`.`ano` ORDER BY `p`.`ano` ASC, sum(`p`.`habitantes`) DESC ;

--
-- Índices para tabelas despejadas
--

--
-- Índices de tabela `estado`
--
ALTER TABLE `estado`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_estado_sigla` (`sigla`),
  ADD UNIQUE KEY `uk_estado_nome` (`nome`),
  ADD KEY `fk_estado_regiao` (`id_regiao`);

--
-- Índices de tabela `municipio`
--
ALTER TABLE `municipio`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_municipio_estado_nome` (`id_estado`,`nome`);

--
-- Índices de tabela `populacao`
--
ALTER TABLE `populacao`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_municipio_ano` (`id_municipio`,`ano`);

--
-- Índices de tabela `regiao`
--
ALTER TABLE `regiao`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT para tabelas despejadas
--

--
-- AUTO_INCREMENT de tabela `estado`
--
ALTER TABLE `estado`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de tabela `municipio`
--
ALTER TABLE `municipio`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT de tabela `populacao`
--
ALTER TABLE `populacao`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=71;

--
-- AUTO_INCREMENT de tabela `regiao`
--
ALTER TABLE `regiao`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Restrições para tabelas despejadas
--

--
-- Restrições para tabelas `estado`
--
ALTER TABLE `estado`
  ADD CONSTRAINT `fk_estado_regiao` FOREIGN KEY (`id_regiao`) REFERENCES `regiao` (`id`);

--
-- Restrições para tabelas `municipio`
--
ALTER TABLE `municipio`
  ADD CONSTRAINT `fk_municipio_estado` FOREIGN KEY (`id_estado`) REFERENCES `estado` (`id`) ON UPDATE CASCADE;

--
-- Restrições para tabelas `populacao`
--
ALTER TABLE `populacao`
  ADD CONSTRAINT `fk_populacao_municipio` FOREIGN KEY (`id_municipio`) REFERENCES `municipio` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
