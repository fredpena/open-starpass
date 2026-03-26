# Open Star Pass

<img width="2560" height="1350" alt="open-starpass" src="https://github.com/user-attachments/assets/21f36ef4-9ca5-4420-94b9-d75f43a69116" />


Open Star Pass is a Vaadin + Spring Boot application that explores a premium space-travel backoffice experience. The current product surface focuses on three admin-style areas:

- `Bookings`
- `Admin / Users`
- `Admin / Planets`

The project is **100% inspired by the Vaadin Starpass demo**: [starpass.demo.vaadin.com](https://starpass.demo.vaadin.com/).

This repository is not trying to be a pixel-perfect clone. The goal is to study, re-interpret and extend that visual language into a more code-first Vaadin Flow application with reusable patterns, richer view logic, and local persistence.

## What This Project Includes

- Vaadin Flow + Spring Boot application
- H2 database for local development
- Seeded demo data for users, planets, bookings and customers
- Master-detail admin flows with route-driven detail panels
- Secondary navigation patterns for admin and bookings
- A custom theme focused on a polished, modern SaaS-style UI

## Current Scope

### Bookings
- `Upcoming`
- `Customers`

### Admin
- `Users`
- `Planets`

These views already include a more realistic UI flow than a static mock:

- searchable lists
- route-aware selection
- detail panels
- contextual actions
- seeded local data

## Tech Stack

- Java
- Spring Boot
- Vaadin Flow
- Maven
- H2

## Running Locally

Start the application with:

```bash
./mvnw spring-boot:run
```

Then open:

```text
http://localhost:8080
```

If you prefer a packaged build:

```bash
./mvnw clean package
java -jar target/openstarpass-1.0-SNAPSHOT.jar
```

For a production build:

```bash
./mvnw clean package -Pproduction
```

## Project Structure

- `src/main/java/dev/fredpena/views`
  Vaadin Flow views and layout composition

- `src/main/java/dev/fredpena/admin/data`
  Admin-side entities, repositories, seed data and services

- `src/main/java/dev/fredpena/bookings/data`
  Booking-side entities, repositories, seed data and services

- `src/main/frontend/themes/openstarpass`
  Theme, layout styling and custom UI polish

## Design Intent

This project is intentionally design-forward. The UI work is centered on:

- elegant sidebar navigation
- list/detail workflows
- refined spacing and hierarchy
- premium visual treatment without losing Vaadin practicality
- reusing Flow components while avoiding generic CRUD aesthetics

## Why This Exists

Open Star Pass is a practical exercise in building a serious-looking Vaadin application that feels closer to a production-ready product than to a starter template.

It is especially useful if you want to study:

- how to structure a themed Vaadin Flow app
- how to evolve mock screens into real flows
- how to connect elegant UI patterns with Java-first views
- how to use H2-backed dummy data while shaping product UX

## Inspiration

The visual and product inspiration comes directly from:

- [Vaadin Starpass Demo](https://starpass.demo.vaadin.com/)

If you know that demo, you will recognize the direction immediately. This repository is a reinterpretation built to learn, extend and experiment on top of that idea.

## Notes

- The data is local demo data meant for development and UI exploration.
- Some actions are already wired to real state changes, while others are still staged as future flows.
- The application is under active iteration.
