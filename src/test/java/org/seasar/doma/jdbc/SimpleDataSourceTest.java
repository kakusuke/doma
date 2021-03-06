/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class SimpleDataSourceTest extends TestCase {

    public void testUrlIsNull() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        dataSource.setUser("user");
        dataSource.setPassword("password");
        try {
            dataSource.getConnection();
            fail();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void testNoSuitableDriverFound() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        dataSource.setUser("user");
        dataSource.setPassword("password");
        dataSource.setUrl("url");
        try {
            dataSource.getConnection();
            fail();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void testIsWrapperFor() throws Exception {
        DataSource dataSource = new SimpleDataSource();
        assertTrue(dataSource.isWrapperFor(SimpleDataSource.class));
        assertFalse(dataSource.isWrapperFor(Runnable.class));
    }

    public void testUnwrap() throws Exception {
        DataSource dataSource = new SimpleDataSource();
        assertNotNull(dataSource.unwrap(SimpleDataSource.class));
        try {
            dataSource.unwrap(Runnable.class);
            fail();
        } catch (SQLException ignored) {
        }
    }

}
