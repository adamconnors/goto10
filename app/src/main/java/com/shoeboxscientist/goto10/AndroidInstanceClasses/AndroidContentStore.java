package com.shoeboxscientist.goto10.AndroidInstanceClasses;

import com.shoeboxscientist.goto10.ContentStore;

import java.io.IOException;

/**
 * Created by adamconnors on 5/26/17.
 */

public class AndroidContentStore implements ContentStore {
    @Override
    public boolean savescript(String script) throws IOException {
        return false;
    }

    @Override
    public String readscript() throws IOException {
        return null;
    }
}
