package org.location;

import org.location.utils.Info;

import java.util.ArrayList;
import java.util.List;

public class DataGenerator {
    private List<Info> infoList;

    DataGenerator() {
        infoList = new ArrayList<>();
    }

    public void addInfo(Info info) {
        infoList.add(info);
    }

    public List<Info> getInfoList() {
        return this.infoList;
    }

    public void clear() {
        this.infoList.clear();
    }
}
