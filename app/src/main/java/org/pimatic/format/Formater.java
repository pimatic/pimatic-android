package org.pimatic.format;

import android.renderscript.Sampler;
import android.util.Log;

import java.text.NumberFormat;
import java.util.HashMap;

/**
 * Created by h3llfire on 16.05.15.
 */
public class Formater {

    private static Scale siScale = new Scale(
        "y,z,a,f,p,n,µ,m,,k,M,G,T,P,E,Z,Y".split(","),
        1000, // Base.
        -8   // Exponent for the first value.
    );

    private static Scale wScale = new Scale(
        ",k".split(","),
        1000,
        0
    );

    private static Scale mScale = new Scale(
        "m,c,d,".split(","),
        10,
        -3
    );

    private static HashMap<String, Scale> scales = new HashMap<String, Scale>() {
        {
            this.put("B", siScale);
            this.put("W", wScale);
            this.put("Wh", wScale);
            this.put("m", mScale);
        }
    };

    public static ValueWithUnit formatValue(final double value, final String unit) {
        ValueWithUnit result = new ValueWithUnit(value, unit);
        Scale scale = scales.get(unit);
        if(scale != null) {
            int index = scale.findIndexOfPrefix(value);
            result.value = scale.getValueInPrefix(index, value);
            result.prefix = scale.getPrefix(index);
        }

        double decimals = 100.0;
        if(unit.equals("W") || unit.equals("Wh")) {
            decimals = 1000.0;
        }
        result.value = Math.round(result.value * decimals) / decimals;
        return result;
    }

    public static class ValueWithUnit{
        public double value;
        public String prefix;
        public String unit;

        public ValueWithUnit(double value, String unit) {
            this.value = value;
            this.prefix = "";
            this.unit = unit;
        }

        public String getFormatedValue() {
            NumberFormat nf = NumberFormat.getInstance();
            return nf.format(value);
        }

        public String getPrefixedUnit() {
            return this.prefix + this.unit;
        }

        public String toString() {
            Log.v("Test2", ""+this.value);
            NumberFormat nf = NumberFormat.getInstance();
            return getFormatedValue() + " " + getPrefixedUnit();
        }
    }

    public static class Scale {
        public String[] prefixes;
        public int base;
        public int expontentFirst;

        public Scale(String[] prefixes, int base, int expontentFirst) {
            this.prefixes = prefixes;
            this.base = base;
            this.expontentFirst = expontentFirst;
        }

        // Binary search to find the greatest index which has a value <= 0
        public int findIndexOfPrefix(double value) {
            int low = 0;
            int high = prefixes.length - 1;
            while (low != high) {
                int mid = (low + high + 1) / 2;
                double current =  getFactor(mid);
                if (current > value) {
                    high = mid - 1;
                } else {
                    low = mid;
                }
            }
            return low;
        }

        public double getFactor(int index) {
            return Math.pow(base, expontentFirst + index);
        }

        public String getPrefix(int index) {
            return prefixes[index];
        }

        public double getValueInPrefix(int index, double value) {
            double factor = getFactor(index);
            return value / factor;
        }

    }
}
