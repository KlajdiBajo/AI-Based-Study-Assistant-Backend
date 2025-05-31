package com.aistudyassistant.backend.AI_Study_Assistant_Backend.mappers;

public interface Mapper<A,B> {

    B mapTo(A a);

    A mapFrom(B b);

}
