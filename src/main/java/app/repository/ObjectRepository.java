package main.java.app.repository;

import main.java.app.domain.Blob;

public interface ObjectRepository {
    String writeObject(Blob blob);
}
