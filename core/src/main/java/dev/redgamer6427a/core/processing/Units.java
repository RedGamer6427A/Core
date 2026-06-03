package dev.redgamer6427a.core.processing;


/**
 * A comprehensive unit library for scientific, engineering, and everyday calculations.
 * <p>
 * This library defines multiple unit types as enums, each implementing the {@link Unit} interface.
 * Units are stored as ratios relative to a base unit for their category (e.g., meters for distance,
 * seconds for time, kilograms for mass, etc.), allowing conversion within the same category.
 * </p>
 *
 * <p><b>Included Unit Types:</b></p>
 *
 * <ul>
 *   <li><b>Time</b> – Sub-second, civil, geological, and light-based units
 *     <ul>
 *       <li>YOCTOSECOND, ZEPTOSECOND, ATTOSECOND, FEMTOSECOND, PICOSECOND, NANOSECOND, MICROSECOND, MILLISECOND</li>
 *       <li>SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR, DECADE, CENTURY, MILLENNIUM</li>
 *       <li>KILOYEAR, MEGAYEAR, GIGAYEAR, TERAYEAR</li>
 *       <li>LIGHT_SECOND, LIGHT_MINUTE, LIGHT_HOUR, LIGHT_DAY, LIGHT_YEAR, LIGHT_KILOYEAR, LIGHT_MEGAYEAR, LIGHT_GIGAYEAR</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Distance</b> – Metric, Imperial, and scientific/astronomical units
 *     <ul>
 *       <li>MILLIMETER, CENTIMETER, METER, KILOMETER</li>
 *       <li>INCH, FOOT, YARD, MILE</li>
 *       <li>ASTRONOMICAL_UNIT, LIGHT_SECOND, LIGHT_MINUTE, LIGHT_HOUR, LIGHT_DAY, LIGHT_YEAR, PARSEC</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Temperature</b> – Metric, Imperial, and scientific units
 *     <ul>
 *       <li>CELSIUS, KELVIN, FAHRENHEIT, RANKINE</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Mass</b> – Metric, Imperial, and scientific units
 *     <ul>
 *       <li>MILLIGRAM, GRAM, KILOGRAM, TONNE</li>
 *       <li>OUNCE, POUND, STONE, TON</li>
 *       <li>ATOMIC_MASS_UNIT, EARTH_MASS, SOLAR_MASS</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Volume</b> – Metric, Imperial, and scientific units
 *     <ul>
 *       <li>MILLILITER, LITER, CUBIC_METER</li>
 *       <li>TEASPOON, TABLESPOON, FLUID_OUNCE, CUP, PINT, QUART, GALLON</li>
 *       <li>CUBIC_KILOMETER, CUBIC_ASTRONOMICAL_UNIT</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Speed</b> – Metric, imperial, nautical, and physics constants
 *     <ul>
 *       <li>METER_PER_SECOND, KILOMETER_PER_HOUR, MILE_PER_HOUR, KNOT, SPEED_OF_LIGHT</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Energy</b> – Metric, imperial, and physics units
 *     <ul>
 *       <li>JOULE, KILOJOULE, CALORIE, KILOCALORIE, ELECTRONVOLT, BTU</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Pressure</b> – Metric, imperial, and standard units
 *     <ul>
 *       <li>PASCAL, KILOPASCAL, BAR, ATMOSPHERE, PSI</li>
 *     </ul>
 *   </li>
 *
 *   <li><b>Power</b> – Metric and imperial units
 *     <ul>
 *       <li>WATT, KILOWATT, HORSEPOWER</li>
 *     </ul>
 *   </li>
 *
 * </ul>
 *
 * <p>All units support conversion within their category using the {@link Unit#convertTo(double, Unit)} method.
 * Temperature units handle offsets as well as ratios. This library is intended for scientific, engineering,
 * and everyday calculations, providing a consistent interface for working with multiple unit types.</p>
 */

public class Units {

    private Units(){}
    /**
     * Base interface for all units.
     */
    public interface Unit {
        double getRatio(); // ratio to base unit

        default double convertTo(double value, Unit target) {
            if (this.getClass() != target.getClass()) {
                throw new IllegalArgumentException("Cannot convert between different unit types.");
            }
            return value * this.getRatio() / target.getRatio();
        }
    }

    /**
     * Time units: sub-second, civil, geological, light-based
     */
    public enum Time implements Unit {
        // Sub-second units
        YOCTOSECOND(1e-24),
        ZEPTOSECOND(1e-21),
        ATTOSECOND(1e-18),
        FEMTOSECOND(1e-15),
        PICOSECOND(1e-12),
        NANOSECOND(1e-9),
        MICROSECOND(1e-6),
        MILLISECOND(1e-3),
        SECOND(1),

        // Civil units
        MINUTE(60),
        HOUR(3600),
        DAY(86400),
        WEEK(604800),
        MONTH(2629746),   // avg month in seconds
        YEAR(31556952),   // avg year
        DECADE(315569520),
        CENTURY(3155695200L),
        MILLENNIUM(31556952000L),

        // Geological / astronomical
        KILOYEAR(3.1556952e10),
        MEGAYEAR(3.1556952e13),
        GIGAYEAR(3.1556952e16),
        TERAYEAR(3.1556952e19),

        // Light-based
        LIGHT_SECOND(1),
        LIGHT_MINUTE(60),
        LIGHT_HOUR(3600),
        LIGHT_DAY(86400),
        LIGHT_YEAR(31556952),
        LIGHT_KILOYEAR(3.1556952e10),
        LIGHT_MEGAYEAR(3.1556952e13),
        LIGHT_GIGAYEAR(3.1556952e16);

        private final double ratio;

        Time(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Distance units: Metric, Imperial, Scientific
     */
    public enum Distance implements Unit {
        // Metric
        MILLIMETER(0.001),
        CENTIMETER(0.01),
        METER(1),
        KILOMETER(1000),

        // Imperial / US
        INCH(0.0254),
        FOOT(0.3048),
        YARD(0.9144),
        MILE(1609.344),

        // Scientific / astronomical
        ASTRONOMICAL_UNIT(1.495978707e11), // meters
        LIGHT_SECOND(2.99792458e8),
        LIGHT_MINUTE(1.798754748e10),
        LIGHT_HOUR(1.079252849e12),
        LIGHT_DAY(2.590206837e13),
        LIGHT_YEAR(9.4607e15),
        PARSEC(3.0857e16);

        private final double ratio;

        Distance(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Temperature units
     */
    public enum Temperature implements Unit {
        CELSIUS(1),
        KELVIN(1),
        FAHRENHEIT(5.0/9),   // Fahrenheit -> Celsius ratio
        RANKINE(5.0/9);      // Rankine -> Celsius ratio

        private final double ratio;

        Temperature(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }

        // Conversion requires offset, not just ratio
        public double convertTo(double value, Temperature target) {
            if (this == CELSIUS) {
                if (target == CELSIUS) return value;
                if (target == KELVIN) return value + 273.15;
                if (target == FAHRENHEIT) return value * 9/5 + 32;
                if (target == RANKINE) return (value + 273.15) * 9/5;
            } else if (this == KELVIN) {
                if (target == CELSIUS) return value - 273.15;
                if (target == KELVIN) return value;
                if (target == FAHRENHEIT) return (value - 273.15) * 9/5 + 32;
                if (target == RANKINE) return value * 9/5;
            } else if (this == FAHRENHEIT) {
                if (target == CELSIUS) return (value - 32) * 5/9;
                if (target == KELVIN) return (value - 32) * 5/9 + 273.15;
                if (target == FAHRENHEIT) return value;
                if (target == RANKINE) return value + 459.67;
            } else if (this == RANKINE) {
                if (target == CELSIUS) return (value - 491.67) * 5/9;
                if (target == KELVIN) return value * 5/9;
                if (target == FAHRENHEIT) return value - 459.67;
                if (target == RANKINE) return value;
            }
            throw new IllegalArgumentException("Unknown temperature conversion");
        }
    }

    /**
     * Mass units: Metric, Imperial, Scientific
     */
    public enum Mass implements Unit {
        // Metric
        MILLIGRAM(1e-6),
        GRAM(0.001),
        KILOGRAM(1),
        TONNE(1000),

        // Imperial
        OUNCE(0.028349523125),
        POUND(0.45359237),
        STONE(6.35029318),
        TON(907.18474),

        // Scientific
        ATOMIC_MASS_UNIT(1.66053906660e-27),
        EARTH_MASS(5.9722e24),
        SOLAR_MASS(1.98847e30);

        private final double ratio;

        Mass(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Volume units
     */
    public enum Volume implements Unit {
        // Metric
        MILLILITER(0.001),
        LITER(1),
        CUBIC_METER(1000),

        // Imperial / US
        TEASPOON(0.00492892159),
        TABLESPOON(0.0147867648),
        FLUID_OUNCE(0.0295735296),
        CUP(0.24),
        PINT(0.473176473),
        QUART(0.946352946),
        GALLON(3.785411784),

        // Scientific
        CUBIC_KILOMETER(1e12),
        CUBIC_ASTRONOMICAL_UNIT(3.348e33); // approx

        private final double ratio;

        Volume(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Speed units
     */
    public enum Speed implements Unit {
        METER_PER_SECOND(1),
        KILOMETER_PER_HOUR(1.0/3.6),
        MILE_PER_HOUR(0.44704),
        KNOT(0.514444),
        SPEED_OF_LIGHT(299792458); // m/s

        private final double ratio;

        Speed(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Energy units
     */
    public enum Energy implements Unit {
        JOULE(1),
        KILOJOULE(1000),
        CALORIE(4.184),
        KILOCALORIE(4184),
        ELECTRONVOLT(1.602176634e-19),
        BTU(1055.05585);

        private final double ratio;

        Energy(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Pressure units
     */
    public enum Pressure implements Unit {
        PASCAL(1),
        KILOPASCAL(1000),
        BAR(100000),
        ATMOSPHERE(101325),
        PSI(6894.75729);

        private final double ratio;

        Pressure(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }

    /**
     * Power units
     */
    public enum Power implements Unit {
        WATT(1),
        KILOWATT(1000),
        HORSEPOWER(745.699872);

        private final double ratio;

        Power(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getRatio() {
            return ratio;
        }
    }


}
