package ru.projectsos.projectsos.models.converter;

public abstract class AbstractConverter<From, To> {

    public abstract To convert(From from);

}
