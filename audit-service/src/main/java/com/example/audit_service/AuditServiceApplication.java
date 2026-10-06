package com.example.audit_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuditServiceApplication {

	public static void main(String[] args) {
		// ⚠️ DEBUG TAM - XOA SAU KHI XONG
		System.out.println("=========================================");
		System.out.println(">>> AUDIT_DB_URL      = " + System.getenv("AUDIT_DB_URL"));
		System.out.println(">>> AUDIT_DB_USERNAME = " + System.getenv("AUDIT_DB_USERNAME"));
		System.out.println(">>> AUDIT_DB_PASSWORD = " + System.getenv("AUDIT_DB_PASSWORD"));
		System.out.println(">>> KAFKA_BOOTSTRAP_SERVERS = " + System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
		System.out.println("=========================================");
		SpringApplication.run(AuditServiceApplication.class, args);
	}

}
