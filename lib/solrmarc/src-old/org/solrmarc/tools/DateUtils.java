package org.solrmarc.tools;

import java.text.*;
import java.util.Calendar;
import java.util.regex.*;

import org.apache.log4j.Logger;

public class DateUtils {

    /**
     * Default Constructor,  private, so it can't be instantiated by other objects
     */    
    private DateUtils(){ }

    private final static Pattern FOUR_DIGIT_PATTERN_BRACES = Pattern.compile("\\[[12]\\d{3,3}\\]");
    private final static Pattern FOUR_DIGIT_PATTERN_ONE_BRACE = Pattern.compile("\\[[12]\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_STARTING_WITH_1_2 = Pattern.compile("(20|19|18|17|16|15)[0-9][0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_1 = Pattern.compile("l\\d{3,3}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_2 = Pattern.compile("\\[19\\]\\d{2,2}");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_3 = Pattern.compile("(20|19|18|17|16|15)[0-9][-?0-9]");
    private final static Pattern FOUR_DIGIT_PATTERN_OTHER_4 = Pattern.compile("i.e. (20|19|18|17|16|15)[0-9][0-9]");
    private final static Pattern BC_DATE_PATTERN = Pattern.compile("[0-9]+ [Bb][.]?[Cc][.]?");
    private final static Pattern FOUR_DIGIT_PATTERN = Pattern.compile("\\d{4,4}");
    private static Matcher matcher;
    private static Matcher matcher_braces;
    private static Matcher matcher_one_brace;
    private static Matcher matcher_start_with_1_2;
    private static Matcher matcher_l_plus_three_digits;
    private static Matcher matcher_bracket_19_plus_two_digits;
    private static Matcher matcher_ie_date;
    private static Matcher matcher_bc_date;
    private static Matcher matcher_three_digits_plus_unk;
    private final static DecimalFormat timeFormat = new DecimalFormat("00.00");
    protected static Logger logger = Logger.getLogger(Utils.class.getName());    

    /**
     * Cleans non-digits from a String
     * @param date String to parse
     * @return Numeric part of date String (or null)
     */
    public static String cleanDate(final String date)
    {
        matcher_braces = FOUR_DIGIT_PATTERN_BRACES.matcher(date);
        matcher_one_brace = FOUR_DIGIT_PATTERN_ONE_BRACE.matcher(date);
        matcher_start_with_1_2 = FOUR_DIGIT_PATTERN_STARTING_WITH_1_2.matcher(date);
        matcher_l_plus_three_digits = FOUR_DIGIT_PATTERN_OTHER_1.matcher(date);
        matcher_bracket_19_plus_two_digits = FOUR_DIGIT_PATTERN_OTHER_2.matcher(date);
        matcher_three_digits_plus_unk = FOUR_DIGIT_PATTERN_OTHER_3.matcher(date);
        matcher_ie_date = FOUR_DIGIT_PATTERN_OTHER_4.matcher(date);
        matcher = FOUR_DIGIT_PATTERN.matcher(date);
        matcher_bc_date = BC_DATE_PATTERN.matcher(date);
        
        String cleanDate = null; // raises DD-anomaly
        
        if(matcher_braces.find())
        {   
            cleanDate = matcher_braces.group();
            cleanDate = Utils.removeOuterBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        } 
        else if (matcher_ie_date.find())
        {
            cleanDate = matcher_ie_date.group().replaceAll("i.e. ", "");
        }
        else if(matcher_one_brace.find())
        {   
            cleanDate = matcher_one_brace.group();
            cleanDate = Utils.removeOuterBrackets(cleanDate);
            if (matcher.find())
            {
                String tmp = matcher.group();
                if (!tmp.equals(cleanDate))
                {
                    tmp = "" + tmp;
                }
            }
        }
        else if(matcher_bc_date.find())
        {   
            cleanDate = null;
        } 
        else if(matcher_start_with_1_2.find())
        {   
            cleanDate = matcher_start_with_1_2.group();
        } 
        else if(matcher_l_plus_three_digits.find())
        {   
            cleanDate = matcher_l_plus_three_digits.group().replaceAll("l", "1");
        } 
        else if(matcher_bracket_19_plus_two_digits.find())
        {   
            cleanDate = matcher_bracket_19_plus_two_digits.group().replaceAll("\\[", "").replaceAll("\\]", "");
        } 
        else if(matcher_three_digits_plus_unk.find())
        {   
            cleanDate = matcher_three_digits_plus_unk.group().replaceAll("[-?]", "0");
        } 
        if (cleanDate != null)
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            String thisYear = dateFormat.format(calendar.getTime());
            try {
                if (Integer.parseInt(cleanDate) > Integer.parseInt(thisYear) + 1) 
                    cleanDate = null;
            }
            catch (NumberFormatException nfe)
            {
                cleanDate = null;
            }
        }
        if (cleanDate != null)
        {
            logger.debug("Date : "+ date + " mapped to : "+ cleanDate);            
        }
        else
        {
            logger.debug("No Date match: "+ date);
        }
        return cleanDate;
    }
    
    /**
     * Calculate time from milliseconds
     * @param totalTime Time in milliseconds
     * @return Time in the format mm:ss.ss
     */
    public static String calcTime(final long totalTime)
    {
        return totalTime / 60000 + ":" + timeFormat.format((totalTime % 60000 ) / 1000);
    }
    

}
