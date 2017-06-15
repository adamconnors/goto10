package com.shoeboxscientist.goto10;

import java.io.IOException;

/**
 * Interface for saving content (e.g. scripts)
 */

public interface ContentStore {
    boolean savescript(String script) throws IOException;
    String readscript() throws IOException;
}
