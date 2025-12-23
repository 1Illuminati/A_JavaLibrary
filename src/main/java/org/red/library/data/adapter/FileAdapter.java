package org.red.library.data.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.red.library.data.serialize.SerializeDataMap;

public class FileAdapter implements IAdapter {
    private final File directory;

    public FileAdapter(String directory) {
        this(new File(directory));
    }

    public FileAdapter(File directory) {
        if (!directory.exists())
            directory.mkdirs();
        if (!directory.isDirectory())
            throw new IllegalArgumentException("file must be directory");

        this.directory = directory;
    }

    private File getFile(String key) {
        return new File(directory, key + ".dat");
    }

    @Override
    public SerializeDataMap loadDataMap(String key) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFile(key)))) {
            return (SerializeDataMap) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
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

    @Override
    public Set<String> loadAllKey() {
        Set<String> keys = new HashSet<>();
        File[] files = this.directory.listFiles();

        if (files == null) return keys;

        for (File file : files) {
            if (!file.isFile() || !file.getName().contains(".dat")) continue;
            keys.add(file.getName().substring(0, file.getName().length() - 4)); 
        }

        return keys;
    }
}
