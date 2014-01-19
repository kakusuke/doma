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
package org.seasar.doma.internal.apt.dao;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.seasar.doma.Dao;
import org.seasar.doma.ResultHandlerType;
import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.Select;
import org.seasar.doma.internal.apt.entity.Emp;

import example.domain.PhoneNumber;

/**
 * @author taedium
 * 
 */
@Dao(config = MyConfig.class)
public interface StreamDao {

    @Select(resultHandler = ResultHandlerType.STREAM)
    Integer selectByIdAndName(Integer id, String name,
            Function<Stream<Emp>, Integer> mapper);

    @Select(resultHandler = ResultHandlerType.STREAM)
    <R> R selectById(Integer id, Function<Stream<PhoneNumber>, R> mapper);

    @Select(resultHandler = ResultHandlerType.STREAM)
    <R extends Number> R select(Function<Stream<String>, R> mapper);

    @Select(resultHandler = ResultHandlerType.STREAM)
    String selectWithHogeFunction(HogeFunction mapper);

    @Select(resultHandler = ResultHandlerType.STREAM, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
    <R> R selectByIdAsMap(Integer id,
            Function<Stream<Map<String, Object>>, R> callback);

    public class HogeFunction implements Function<Stream<String>, String> {

        @Override
        public String apply(Stream<String> t) {
            return null;
        }

    }
}
