#! /bin/bash
# index_incr_sirsi.sh
# Remove deleted records from index and update index per marc records given
#  Naomi Dushay 2010-01-08

#SOLR_DATA_DIR=/data/solr/dataBuild
SOLR_DATA_DIR=/data/solr

# get filename date
if [ $1 ] ; then
  COUNTS_FNAME=$1"_dates_counts"
  DEL_KEYS_FNAME=$1"_ckeys_delete.del"
  RECORDS_FNAME=$1"_uni_increment.marc"
else
  TODAY=`eval date +%y%m%d`
  COUNTS_FNAME=$TODAY"_dates_counts"
  DEL_KEYS_FNAME=$TODAY"_ckeys_delete.del"
  RECORDS_FNAME=$TODAY"_uni_increment.marc"
fi

RAW_DATA_DIR=/data/sirsi/latest/updates
REC_FNAME=$RAW_DATA_DIR/$RECORDS_FNAME

DEL_ARG="-Dmarc.ids_to_delete="$RAW_DATA_DIR/$DEL_KEYS_FNAME

JAVA_HOME=/usr/lib/jvm/java

# set up the classpath
BLACKLIGHT_HOMEDIR=/home/blacklight
SOLRMARC_BASEDIR=$BLACKLIGHT_HOMEDIR/solrmarc
DIST_DIR=$SOLRMARC_BASEDIR/dist
SITE_JAR=$DIST_DIR/StanfordIndexer.jar
CORE_JAR=$DIST_DIR/SolrMarc.jar
CP=$SITE_JAR:$CORE_JAR:$DIST_DIR

# create log directory
LOG_PARENT_DIR=$RAW_DATA_DIR/logs
LOG_DIR=$LOG_PARENT_DIR
mkdir -p $LOG_DIR

# index the files
nohup java -Xmx16g -Xms16g -Dsolr.data.dir=$SOLR_DATA_DIR $DEL_ARG -Dsolr.optimize_at_end="true" -cp $CP -jar $CORE_JAR $REC_FNAME &>$LOG_DIR/$RECORDS_FNAME".txt"

exit 0
