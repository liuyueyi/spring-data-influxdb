/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.influxdb;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private InfluxDBTemplate<Point> influxDBTemplate;

    @Data
    @Measurement(database = "yhh", name = "cpu")
    public static class CPU {
        @Column(name = "time")
        private String time;

        @Column(name = "tenant", tag = true)
        private String tenant;

        @Column(name = "idle")
        private Long idle;
        @Column(name = "user")
        private Long user;
        @Column(name = "system")
        private Long system;
        @Column(name = "price")
        private BigDecimal price;
    }

    @Override
    public void run(final String... args) throws Exception {
        // Create database...
        influxDBTemplate.createDatabase();

        // Create some data...
        final Point p1 = Point.measurement("cpu").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("tenant", "default").addField("idle", 90L).addField("user", 9L).addField("system", 1L)
                .addField("price", new BigDecimal(100.24).setScale(4, RoundingMode.CEILING)).build();

        final Point p2 = Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("tenant", "default").addField("used", 80L).addField("free", 1L)
                .addField("price", new BigDecimal(67.72).setScale(4, RoundingMode.CEILING)).build();
        influxDBTemplate.write(p1, p2);

        List<CPU> result = influxDBTemplate
                .queryForObject(new Query("select * from cpu", influxDBTemplate.getDatabase()), CPU.class);
        System.out.println(result);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
