-- phpMyAdmin SQL Dump
-- version 3.5.2.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Oct 26, 2013 at 11:52 PM
-- Server version: 5.5.27-log
-- PHP Version: 5.4.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `cd`
--
CREATE DATABASE `cd` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `cd`;

-- --------------------------------------------------------

--
-- Table structure for table `emailqueue`
--

CREATE TABLE IF NOT EXISTS `emailqueue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from_email_address` varchar(255) NOT NULL,
  `to_email_address` varchar(255) NOT NULL,
  `subject` mediumtext NOT NULL,
  `body` mediumtext NOT NULL,
  `dirty_bit` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `emailqueue`
--

INSERT INTO `emailqueue` (`id`, `from_email_address`, `to_email_address`, `subject`, `body`, `dirty_bit`) VALUES
(1, 'test@gmail.com', 'test@live.com', 'Good News EveryOne', 'Bird is the Word!', 0),
(2, 'asdf@gmail.com', 'zxcv@gmail.com', 'News Letter', 'Awe Yeah!', 0),
(3, 'qazxsw2@gmail.com', 'asdfgh@gmail.com', 'SMTP jaanch Sandesh', 'fsafdasf', 0);

-- --------------------------------------------------------

--
-- Table structure for table `mc`
--

CREATE TABLE IF NOT EXISTS `mc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `val` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `mc`
--

INSERT INTO `mc` (`id`, `val`) VALUES
(1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `run`
--

CREATE TABLE IF NOT EXISTS `run` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `val` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `run`
--

INSERT INTO `run` (`id`, `val`) VALUES
(1, 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
