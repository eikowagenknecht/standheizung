package de.phenx.standheizung.util;

public class ThermoCall {

    // https://www.webasto.com/fileadmin/webasto_files/documents/country-folder/germany/car/technical-documentation/germany-car-technical-documentation-thermo-call-4.pdf

    /**
     * Turn on the Heater
     */
    public static String TURN_ON = "START";

    /**
     * Turn off the Heater
     */
    public static String TURN_OFF = "STOP";

    /**
     * Turn on the Heater at the specified time, 24h format (%1).
     *
     * Example: %1 = 1600: Turn on at 4 pm
     */
    public static String TURN_ON_DELAYED = "START%1";

    /**
     * Set the HTM level to %1
     * 0 = No HTM
     * 1 = MIN
     * ...
     * 5 = MAX
     */
    public static String SET_HTM_LEVEL = "HTMLEVEL:%1"; //

    /**
     * Switch to winter mode
     */
    public static String WINTER_MODE = "WINTER"; // Heater

    /**
     * Switch to summer mode
     */
    public static String SUMMER_MODE = "SUMMER"; // Fan only

    /**
     * Display temperature
     */
    public static String TEMPERATURE = "TEMP";

    /**
     * Display status:
     * - Running / Stopped
     * - AUX Output status
     * - Temperature
     * - Battery voltage
     */
    public static String STATUS = "STATUS";

    /**
     * Set the duration to %2 minutes with PIN %1
     *
     * Minimum 5 minutes, maximum 120 minutes.
     * PIN is a 4 digit code (default 0000)
     *
     * Example:
     * %1 = 0000 (default PIN)
     * %2 = 30
     */
    public static String DURATION = "%1TIMER1:%2";
}
