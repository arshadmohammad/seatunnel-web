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

import org.apache.seatunnel.shade.com.typesafe.config.ConfigFactory;

import org.apache.seatunnel.api.table.catalog.CatalogTableUtil;
import org.apache.seatunnel.api.table.catalog.schema.TableSchemaOptions;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.file.config.BaseSourceConfigOptions;
import org.apache.seatunnel.connectors.seatunnel.file.config.FileFormat;
import org.apache.seatunnel.connectors.seatunnel.file.config.HadoopConf;
import org.apache.seatunnel.connectors.seatunnel.file.exception.FileConnectorException;
import org.apache.seatunnel.connectors.seatunnel.file.source.reader.ReadStrategy;
import org.apache.seatunnel.connectors.seatunnel.file.source.reader.ReadStrategyFactory;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.hadoop.fs.FileSystem.FS_DEFAULT_NAME_KEY;

@Slf4j
public class HdfsFileConfiguration {

    public static Configuration getConfiguration(Map<String, String> hadoopFsOptions) {
        Configuration hadoopConf = new Configuration();
        hadoopConf.set(
                FS_DEFAULT_NAME_KEY, hadoopFsOptions.get(HdfsFileOptionRule.DEFAULT_FS.key()));

        if (hadoopFsOptions.containsKey(BaseSourceConfigOptions.HDFS_SITE_PATH.key())) {
            hadoopConf.addResource(
                    new Path(hadoopFsOptions.get(BaseSourceConfigOptions.HDFS_SITE_PATH.key())));
        }

        if (hadoopFsOptions.containsKey(BaseSourceConfigOptions.KERBEROS_PRINCIPAL.key())) {
            hadoopConf.set("hadoop.security.authentication", "kerberos");
            hadoopConf.set(
                    "dfs.namenode.kerberos.principal",
                    hadoopFsOptions.get(BaseSourceConfigOptions.KERBEROS_PRINCIPAL.key()));
        }

        if (hadoopFsOptions.containsKey(BaseSourceConfigOptions.KERBEROS_KEYTAB_PATH.key())) {
            hadoopConf.set(
                    "dfs.namenode.keytab.file",
                    hadoopFsOptions.get(BaseSourceConfigOptions.KERBEROS_KEYTAB_PATH.key()));
        }
        return hadoopConf;
    }

    public static SeaTunnelRowType getSeaTunnelRowType(Map<String, String> hadoopFsOptions) {
        Configuration configuration = getConfiguration(hadoopFsOptions);
        HadoopConf hadoopConf =
                new HadoopConf(hadoopFsOptions.get(HdfsFileOptionRule.DEFAULT_FS.key()));
        hadoopConf.setExtraOptionsForConfiguration(configuration);
        ReadStrategy readStrategy =
                ReadStrategyFactory.of(
                        hadoopFsOptions.get(BaseSourceConfigOptions.FILE_FORMAT_TYPE.key()));
        readStrategy.setPluginConfig(ConfigFactory.parseMap(hadoopFsOptions));
        readStrategy.init(hadoopConf);
        List<String> filePaths;
        String path = hadoopFsOptions.get(BaseSourceConfigOptions.FILE_PATH.key());
        try {
            filePaths = readStrategy.getFileNamesByPath(path);
        } catch (IOException e) {
            String errorMsg = String.format("Get file list from this path [%s] failed", path);
            throw new DataSourcePluginException(errorMsg, e);
        }

        // support user-defined schema
        FileFormat fileFormat =
                FileFormat.valueOf(
                        hadoopFsOptions
                                .get(BaseSourceConfigOptions.FILE_FORMAT_TYPE.key())
                                .toUpperCase());
        // only json text csv type support user-defined schema now
        SeaTunnelRowType rowType;
        if (hadoopFsOptions.containsKey(TableSchemaOptions.SCHEMA.key())) {
            switch (fileFormat) {
                case CSV:
                case TEXT:
                case JSON:
                case EXCEL:
                case XML:
                    SeaTunnelRowType userDefinedSchema =
                            CatalogTableUtil.buildWithConfig(
                                            ConfigFactory.parseMap(hadoopFsOptions))
                                    .getSeaTunnelRowType();
                    readStrategy.setSeaTunnelRowTypeInfo(userDefinedSchema);
                    rowType = readStrategy.getActualSeaTunnelRowTypeInfo();
                    break;
                case ORC:
                case PARQUET:
                case BINARY:
                    throw new DataSourcePluginException(
                            "SeaTunnel does not support user-defined schema for [parquet, orc, binary] files");
                default:
                    // never got in there
                    throw new DataSourcePluginException(
                            "SeaTunnel does not supported this file format");
            }
        } else {
            if (filePaths.isEmpty()) {
                // When the directory is empty, distribute default behavior schema
                rowType = CatalogTableUtil.buildSimpleTextSchema();
            } else {
                try {
                    rowType = readStrategy.getSeaTunnelRowTypeInfo(filePaths.get(0));
                } catch (FileConnectorException e) {
                    String errorMsg =
                            String.format(
                                    "Get table schema from file [%s] failed", filePaths.get(0));
                    throw new DataSourcePluginException(errorMsg, e);
                }
            }
        }
        return rowType;
    }
}
