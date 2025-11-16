package org.red.library.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.red.library.serialize.SerializeDataMap;
public class FileAdapter implements IAdapter {
    private final File directory;

    public FileAdapter(String directory) {
        this(new File(directory));
    }

    private File getFile(String key) {
        return new File(directory, key + ".dat");
    }

    public FileAdapter(File directory) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("file must be directory");

        this.directory = directory;
    }

    @Override
    public SerializeDataMap loadDataMap(String key) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFile(key)))) {
            return (SerializeDataMap) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveDataMap(String key, SerializeDataMap map) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFile(key)))) {
            oos.writeObject(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containDataMap(String key) {
        return getFile(key).exists();
    }

    @Override
    public void deleteDataMap(String key) {
        File file = getFile(key);
        if (file.exists()) file.delete();
    }

}
