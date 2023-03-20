#!/bin/bash
mvn -B -ntp -Ppublish-ghcr -DskipTests package spring-boot:build-image
mvn -B -ntp -Pnative -Ppublish-ghcr -DskipTests spring-boot:build-image
