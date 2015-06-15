# Verita
Verita (it: truth) is [Event Sourcing](https://msdn.microsoft.com/en-us/library/dn589792.aspx) for the JVM with
various implementations

[![Build Status](https://api.travis-ci.org/CPGFinanceSystems/verita.svg?branch=master)]
(https://travis-ci.org/CPGFinanceSystems/verita)

## Components

### API

API package without any transitive dependencies which contains all interfaces describing available data and functionality

### Mock implementation

An in-memory implementation of the API suitable for automated integration tests

### Event Store implementation

An implementation using [EventStore](https://www.geteventstore.com) as it's backend via the official
[JVM API](https://github.com/EventStore/EventStore.JVM) using [Akka](http://www.akka.io) in behind

### Event Store Spring integration

An integration library for the [Spring Framework](http://www.spring.io) and [Spring Boot](http://projects.spring.io/spring-boot)
providing configuration management for the Event Store implementation

## Download

Binaries are available on maven central with following GAV:

- groupId: `de.cpg.oss.verita`
- artifacts: `verita-api`, `verita-mock-impl`, `verita-event-store-impl`, `verita-event-store-spring`
- version: `1.0.0`

## TODO / Ideas

* [PostgreSQL](http://www.postgresql.org) 9.3+ based implementation with serialized JSON objects