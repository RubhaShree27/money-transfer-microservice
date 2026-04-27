package org.moneytransfer.moneytransfermicroservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moneytransfer.moneytransfermicroservice.MoneyTransferMicroserviceApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.moneytransfer.moneytransfermicroservice.dto.TransferRequest;

import java.math.BigDecimal;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest(classes = MoneyTransferMicroserviceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransferControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void testGetAllAccounts() {
        get("/api/accounts")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].accountId", notNullValue())
                .body("[0].accountHolder", notNullValue())
                .body("[0].balance", notNullValue());
    }

    @Test
    public void testGetAccountById() {
        get("/api/accounts/ACC001")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("accountId", equalTo("ACC001"))
                .body("accountHolder", equalTo("Rubha shree"))
                .body("balance", notNullValue());
    }

    @Test
    public void testGetAccountNotFound() {
        get("/api/accounts/INVALID_ACCOUNT")
                .then()
                .statusCode(404);
    }

    @Test
    public void testSuccessfulMoneyTransfer() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC001",
                "ACC002",
                new BigDecimal("100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("fromAccountId", equalTo("ACC001"))
                .body("toAccountId", equalTo("ACC002"))
                .body("amount", notNullValue())
                .body("transactionId", notNullValue())
                .body("message", containsString("successfully"));
    }

    @Test
    public void testTransferInsufficientFunds() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC002",
                "ACC001",
                new BigDecimal("10000.00")  // More than ACC002 has
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"))
                .body("message", containsString("Insufficient funds"));
    }

    @Test
    public void testTransferFromNonExistentAccount() {
        TransferRequest transferRequest = new TransferRequest(
                "INVALID_ACCOUNT",
                "ACC001",
                new BigDecimal("100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"))
                .body("message", containsString("not found"));
    }

    @Test
    public void testTransferToNonExistentAccount() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC001",
                "INVALID_ACCOUNT",
                new BigDecimal("100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"))
                .body("message", containsString("not found"));
    }

    @Test
    public void testTransferToSameAccount() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC001",
                "ACC001",
                new BigDecimal("100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"));
    }

    @Test
    public void testTransferWithNegativeAmount() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC001",
                "ACC002",
                new BigDecimal("-100.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"));
    }

    @Test
    public void testTransferWithZeroAmount() {
        TransferRequest transferRequest = new TransferRequest(
                "ACC001",
                "ACC002",
                new BigDecimal("0.00")
        );

        given()
                .contentType(ContentType.JSON)
                .body(transferRequest)
                .when()
                .post("/api/accounts/transfer")
                .then()
                .statusCode(400)
                .body("status", equalTo("FAILED"));
    }

    @Test
    public void testCreateNewAccount() {
        String accountPayload = "{\n" +
                "  \"accountId\": \"ACC999\",\n" +
                "  \"accountHolder\": \"Test User\",\n" +
                "  \"balance\": 5000.00\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPayload)
                .when()
                .post("/api/accounts")
                .then()
                .statusCode(201)
                .body("accountId", equalTo("ACC999"))
                .body("accountHolder", equalTo("Test User"));
    }

    @Test
    public void testCreateDuplicateAccount() {
        String accountPayload = "{\n" +
                "  \"accountId\": \"ACC001\",\n" +
                "  \"accountHolder\": \"Duplicate\",\n" +
                "  \"balance\": 1000.00\n" +
                "}";

        given()
                .contentType(ContentType.JSON)
                .body(accountPayload)
                .when()
                .post("/api/accounts")
                .then()
                .statusCode(400);
    }

    @Test
    public void testMultipleTransfersSequential() {
        // First transfer
        TransferRequest transfer1 = new TransferRequest("ACC001", "ACC002", new BigDecimal("50.00"));
        given()
                .contentType(ContentType.JSON)
                .body(transfer1)
                .post("/api/accounts/transfer")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // Second transfer
        TransferRequest transfer2 = new TransferRequest("ACC002", "ACC003", new BigDecimal("30.00"));
        given()
                .contentType(ContentType.JSON)
                .body(transfer2)
                .post("/api/accounts/transfer")
                .then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));

        // Verify account balances
        get("/api/accounts/ACC001")
                .then()
                .body("balance", notNullValue());
    }
}