package edu.stanford;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.solrmarc.index.IndexTest;

/**
 * Site Specific code used for testing the stanford Bibliographic index
 * @author Naomi Dushay
 *
 */
public abstract class BibIndexTest extends IndexTest {

	// Note:  the hardcodings below are only used when the tests are
	//  invoked without the properties set
	//   the properties ARE set when the tests are invoke via ant.
	{
        solrmarcPath = System.getProperty("solrmarc.path");
        if (solrmarcPath == null)
            solrmarcPath = new File("lib" + File.separator + "solrmarc").getAbsolutePath();
		
		siteSpecificPath = System.getProperty("solrmarc.site.path");
		if (siteSpecificPath == null)
			siteSpecificPath = new File("examples" + File.separator + "stanfordVufind").getAbsolutePath(); 
		
        String configPropDir = System.getProperty("test.config.dir");
        if (configPropDir == null)
            configPropDir = siteSpecificPath;
        
        configPropFile = System.getProperty("test.config.file");
		if (configPropFile == null)
		    configPropFile = configPropDir + File.separator + "bibix_config.properties";
		
		solrPath = System.getProperty("solr.path");
		if (solrPath == null)
			solrPath = siteSpecificPath +  File.separator + "solr";

//		testDir = solrmarcPath + File.separator + "test";
        testDir = "test";
		testDataParentPath = ".." + File.separator + ".." + File.separator + testDir + File.separator + "data";
		testDataPath = testDataParentPath + File.separator + "allfieldsTests.mrc";
		solrDataDir = System.getProperty("solr.data.dir");
		if (solrDataDir == null)
			solrDataDir = solrPath + File.separator + "data";
	}

	
	public void createIxInitVars(String testDataFname) 
		throws ParserConfigurationException, IOException, SAXException 
	{
		createNewTestIndex(testDataParentPath + File.separator + testDataFname, configPropFile, solrPath, solrDataDir, solrmarcPath, siteSpecificPath);
		solrCore = getSolrCore(solrPath, solrDataDir);
		sis = getSolrIndexSearcher(solrCore);
	}

}