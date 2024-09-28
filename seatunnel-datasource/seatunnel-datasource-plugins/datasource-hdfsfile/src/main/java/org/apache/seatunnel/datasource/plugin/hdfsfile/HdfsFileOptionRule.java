/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.datasource.plugin.hdfsfile;

import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY;

public class HdfsFileOptionRule {

    public static final Option<String> DEFAULT_FS =
            Options.key(FS_DEFAULT_NAME_KEY)
                    .stringType()
                    .noDefaultValue()
                    .withDescription("HDFS namenode host");

    public static final Option<String> HDFS_SITE_PATH =
            Options.key("hdfs_site_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The path of hdfs-site.xml");

    public static final Option<String> KERBEROS_PRINCIPAL =
            Options.key("kerberos_principal")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Kerberos principal");

    public static final Option<String> KERBEROS_KEYTAB_PATH =
            Options.key("kerberos_keytab_path")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("Kerberos keytab file path");

    public static final Option<String> KRB5_PATH =
            Options.key("krb5_path")
                    .stringType()
                    .defaultValue("/etc/krb5.conf")
                    .withDescription(
                            "When use kerberos, we should set krb5 path file path such as '/seatunnel/krb5.conf' or use the default path '/etc/krb5.conf");

    public static OptionRule optionRule() {
        return OptionRule.builder()
                .required(DEFAULT_FS)
                .required(HDFS_SITE_PATH)
                .optional(KERBEROS_PRINCIPAL)
                .optional(KERBEROS_KEYTAB_PATH)
                .optional(KRB5_PATH)
                .build();
    }

    public static OptionRule metadataRule() {
        return OptionRule.builder().build();
    }
}
