/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creativecommons.nutch;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.nutch.metadata.CreativeCommons;

import org.apache.nutch.parse.Parse;

import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.IndexingException;
import org.apache.hadoop.io.UTF8;

import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.CreativeCommons;

import java.util.logging.Logger;
import org.apache.hadoop.util.LogFormatter;
import org.apache.hadoop.conf.Configuration;

import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/** Adds basic searchable fields to a document. */
public class CCIndexingFilter implements IndexingFilter {
  public static final Logger LOG
    = LogFormatter.getLogger(CCIndexingFilter.class.getName());

  /** The name of the document field we use. */
  public static String FIELD = "cc";

  private Configuration conf;

  public Document filter(Document doc, Parse parse, UTF8 url, CrawlDatum datum, Inlinks inlinks)
    throws IndexingException {
    
    Metadata metadata = parse.getData().getParseMeta();
    // index the license
    String licenseUrl = metadata.get(CreativeCommons.LICENSE_URL);
    if (licenseUrl != null) {
      LOG.info("CC: indexing " + licenseUrl + " for: " + url.toString());

      // add the entire license as cc:license=xxx
      addFeature(doc, "license=" + licenseUrl);

      // index license attributes extracted of the license url
      addUrlFeatures(doc, licenseUrl);
    }

    // index the license location as cc:meta=xxx
    String licenseLocation = metadata.get(CreativeCommons.LICENSE_LOCATION);
    if (licenseLocation != null) {
      addFeature(doc, "meta=" + licenseLocation);
    }

    // index the work type cc:type=xxx
    String workType = metadata.get(CreativeCommons.WORK_TYPE);
    if (workType != null) {
      addFeature(doc, workType);
    }

    return doc;
  }

  /** Add the features represented by a license URL.  Urls are of the form
   * "http://creativecommons.org/licenses/xx-xx/xx/xx", where "xx" names a
   * license feature. */
  public void addUrlFeatures(Document doc, String urlString) {
    try {
      URL url = new URL(urlString);

      // tokenize the path of the url, breaking at slashes and dashes
      StringTokenizer names = new StringTokenizer(url.getPath(), "/-");

      if (names.hasMoreTokens())
        names.nextToken();                        // throw away "licenses"

      // add a feature per component after "licenses"
      while (names.hasMoreTokens()) {
        String feature = names.nextToken();
        addFeature(doc, feature);
      }
    } catch (MalformedURLException e) {
      LOG.warning("CC: failed to parse url: " + urlString + " : " + e);
    }
  }
  
  private void addFeature(Document doc, String feature) {
    doc.add(Field.Keyword(FIELD, feature));
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return this.conf;
  }

}
