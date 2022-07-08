import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsing {
    String message;
    static Logger LOGGER = LoggerFactory.getLogger(Parsing.class);

    public boolean detectGoodMessage(String mes) {
        String template1 = "(\\b[А-Яа-я]+\\b)";
        String template2 = "([0-9]\\b)";


        Pattern pattern = Pattern.compile(template1);
        Matcher regexp = pattern.matcher(mes);

        if (regexp.find()) {
            pattern = Pattern.compile(template2);
            regexp = pattern.matcher(mes);
            return regexp.find();
        } else {
            return false;
        }
    }

    public String detect(String mes) throws IllegalStateException {
        // пропускает вариант wer24.54 где берет .54 и 123.66в берет 123
        String template = "\\b[-+]?[0-9]*\\.?[0-9]+\\b";
        String s = "";
        Pattern pattern = Pattern.compile(template);
        Matcher sms = pattern.matcher(mes);

        while (sms.find()) {
            s += sms.group();
        }
        return s;
    }

    public String detectWords(String mes) throws IllegalStateException {
        String template = "[а-яА-Я]+";
        String s = "";
        Pattern pattern = Pattern.compile(template);
        Matcher sms = pattern.matcher(mes);

        while (sms.find()) {
            s += " " + sms.group();
        }
        return s;
    }

    public ArrayList<Double> detectNumbers(String s) throws NumberFormatException {
        LOGGER.debug("detectNumbers get string = " + s);
        ArrayList<Double> numbers = new ArrayList<>();
        for (String part : s.split("[-]|[+]")) {
            if (!part.trim().equals("")) {
                numbers.add(Double.parseDouble(part));
            } else {
            }
        }
        return numbers;

    }

    public ArrayList<String> detectOperators(String s) throws NumberFormatException {
        ArrayList<String> operators = new ArrayList<>();
        for (String part : s.split("\\d+[.][0-9]+|\\d+")) {
            operators.add(part);
        }

        return operators;

    }

    public Double calculate(ArrayList<Double> d, ArrayList<String> s) throws IndexOutOfBoundsException {
        Double result = d.get(0);
        for (int i = 0; i < d.size() - 1; i++) {

            if (s.get(i + 1).equals("+")) {
                result += d.get(i + 1);
            } else {
                result -= d.get(i + 1);
            }
        }
        return Double.valueOf((Math.round(result * 100))) / (100);
    }


}
