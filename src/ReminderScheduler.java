public final class ReminderScheduler {
    public static void schedule(int hour, int minute) {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    long delay = millisUntilNext(hour, minute);
                    Thread.sleep(delay);
                    Library.updateOverdueTable();
                    new ReminderJob().run();
                }
                catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }, "reminder-scheduler");
        t.setDaemon(true);
        t.start();
    }
    private static long millisUntilNext(int hour, int minute) {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!next.isAfter(now)) next = next.plusDays(1);
        return java.time.Duration.between(now, next).toMillis();
    }
}
