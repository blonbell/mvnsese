# Usage #

After building from source activate the maven plugin by adding the following to your pom.xml


```

    <build>
        <plugins>
            <plugin>
                <groupId>com.googlecode.mvnsese</groupId>
                <artifactId>selenese-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>integration-test</phase>
                        <goals>
                            <goal>integration-test</goal>
                        </goals>
                        <configuration>
                            <groups>
                                <group>
                                    <baseURL>http://google.com/</baseURL>
                                    <suites>
                                        <suite>src/it/ide/googleSuite.html</suite>
                                    </suites>
                                </group>
                            </groups>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

```