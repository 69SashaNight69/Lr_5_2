import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TicketBooking {

    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.println("=== Ласкаво просимо до Системи Бронювання Квитків ===");

        String destination = askForDestination();
        TransportationType transportation = askForTransportationType();
        int numberOfSeats = askForNumberOfSeats();

        CompletableFuture<BookingResult> bookingProcess = checkAvailability(destination, transportation)
                .thenCompose(availability -> findBestPrice(availability.isAvailable(), destination, transportation, numberOfSeats))
                .thenCompose(bestPrice -> bookTicket(bestPrice.selectedTicket(), numberOfSeats));


        BookingResult result = bookingProcess.get();

        System.out.println("\n=== Процес Бронювання Завершено ===");

        if (result.isSuccess()) {
            System.out.println("Квиток успішно заброньовано! Деталі вашого бронювання:");
            System.out.println("  Місто призначення: " + destination);
            System.out.println("   Вид транспорту: " + transportation);
            System.out.println("    Код бронювання: " + result.getBookingCode());
            System.out.println("   Ціна: " + result.getPrice() + " USD");
            System.out.println("   Кількість місць: " + numberOfSeats);

        } else {
            System.out.println("Бронювання не вдалось, через помилку коду " + result.getBookingCode());
        }

    }

    // Методи взаємодії з користувачем:
    static String askForDestination() {
        System.out.println("Будь ласка, введіть місто призначення, куди ви хочете поїхати:");
        return scanner.nextLine();
    }

    static TransportationType askForTransportationType() {
        while (true) {
            System.out.println("Будь ласка, оберіть вид транспорту:");
            System.out.println(" 1. Літак");
            System.out.println(" 2. Потяг");
            System.out.println(" 3. Автобус");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    return TransportationType.PLANE;
                case 2:
                    return TransportationType.TRAIN;
                case 3:
                    return TransportationType.BUS;
                default:
                    System.out.println("Неправильний вибір, будь ласка, спробуйте ще раз");
            }
        }

    }

    static int askForNumberOfSeats() {
        System.out.println("Будь ласка, введіть кількість місць, яку ви хочете забронювати:");
        return scanner.nextInt();
    }


    // Метод перевірки наявності місць
    static CompletableFuture<AvailabilityCheckResult> checkAvailability(String destination, TransportationType transportation) {
        System.out.println("Перевіряємо наявність місць для подорожі в " + destination + " на " + transportation);
        return CompletableFuture.supplyAsync(() -> {
            delay(getRandomDelay(500, 1000));
            boolean available = random.nextBoolean(); // Імітація наявності місць.
            AvailabilityCheckResult checkResult = new AvailabilityCheckResult(available);
            if (available) {
                System.out.println("Наявність місць підтверджена! - Місця доступні для " + transportation + " до " + destination);
            } else {
                System.out.println("Нажаль, немає вільних місць на " + transportation + " до " + destination);
            }
            return checkResult;
        });

    }


    // Метод пошуку кращої ціни:
    static CompletableFuture<BestPriceResult> findBestPrice(boolean availability, String destination, TransportationType transportation, int numberOfSeats) {
        System.out.println("Пошук найкращої ціни для подорожі до " + destination + " на " + transportation + " для " + numberOfSeats + " місць");

        return CompletableFuture.supplyAsync(() -> {
            delay(getRandomDelay(500, 1000));
            if (!availability) {
                System.out.println("Не вдалось знайти ціни, бо відсутні місця");
                return new BestPriceResult(null, false);
            }
            List<TicketInfo> tickets = generateTicketOptions(destination, transportation);

            System.out.println("Знайдено " + tickets.size() + " варіантів квитків:");
            tickets.forEach(ticket -> {
                System.out.println("   -" + ticket);
            });
            TicketInfo selectedTicket = selectBestAvailableTicket(tickets, numberOfSeats);

            if (selectedTicket == null) {
                System.out.println("Немає доступних квитків з необхідною кількістю місць.");
                return new BestPriceResult(null, false);
            }

            System.out.println("Найкращий варіант квитка: " + selectedTicket);

            return new BestPriceResult(selectedTicket, true);
        });

    }

    // Генерація тестових квитків:
    static List<TicketInfo> generateTicketOptions(String destination, TransportationType type) {
        List<TicketInfo> ticketOptions = new ArrayList<>();
        int numberOfOptions = random.nextInt(3) + 2; // Генеруємо від 2 до 4 варіантів
        for (int i = 0; i < numberOfOptions; i++) {
            int price = getRandomDelay(100, 500);
            int availableSeats = random.nextInt(5) + 1; // Генеруємо від 1 до 5 вільних місць
            ticketOptions.add(new TicketInfo(generateTicketCode(), price, availableSeats));
        }
        return ticketOptions;
    }

    // Вибір квитка з найменшою ціною та наявними місцями
    static TicketInfo selectBestAvailableTicket(List<TicketInfo> tickets, int numberOfSeats) {
        // Сортуємо квитки за ціною від найменшої до найбільшої
        tickets.sort(Comparator.comparingInt(TicketInfo::getPrice));

        // Знаходимо перший квиток із вільними місцями
        for (TicketInfo ticket : tickets) {
            if (ticket.getAvailableSeats() >= numberOfSeats) {
                return ticket;
            }
        }
        return null; // Якщо немає квитків з місцями
    }

    // Метод бронювання квитка:
    static CompletableFuture<BookingResult> bookTicket(TicketInfo selectedTicket, int numberOfSeats) {
        System.out.println("Завершуємо бронювання квитка...");

        return CompletableFuture.supplyAsync(() -> {
            delay(getRandomDelay(500, 1000));
            if (selectedTicket == null) {
                System.out.println("Неможливо продовжити, бо не вибрано квиток");
                return new BookingResult(false, "NO_TICKET");
            }
            boolean successfulBooking = random.nextDouble() > 0.1; // Симулюємо успішність бронювання
            if (successfulBooking) {
                String bookingCode = generateBookingCode();
                System.out.println("Квиток заброньовано! Код бронювання: " + bookingCode);
                // зменшуємо кількість місць на 1
                selectedTicket.availableSeats -= numberOfSeats;
                return new BookingResult(true, bookingCode, selectedTicket.getPrice());
            } else {
                System.out.println("Бронювання не вдалося, спробуйте ще раз");
                return new BookingResult(false, "RESERVE-ERROR");
            }

        });
    }

    // Утилітарні методи
    // Метод генерації коду бронювання
    static String generateBookingCode() {
        String code = "";
        String values = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 8; i++) {
            code += values.charAt(random.nextInt(values.length()));
        }
        return code;
    }

    static String generateTicketCode() {
        String code = "";
        String values = "ABCDEFGHIJ123456789";
        for (int i = 0; i < 10; i++) {
            code += values.charAt(random.nextInt(values.length()));
        }
        return code;
    }


    record BookingResult(boolean isSuccess, String bookingCode, int price) {
        public BookingResult(boolean isSuccess, String bookingCode) {
            this(isSuccess, bookingCode, 0);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getBookingCode() {
            return bookingCode;
        }

        public int getPrice() {
            return price;
        }
    }

    record AvailabilityCheckResult(boolean isAvailable) {
    }

    record BestPriceResult(TicketInfo selectedTicket, boolean isPriceFound) {
    }

    static class TicketInfo {
        private String ticketCode;
        private int price;
        public int availableSeats;

        public TicketInfo(String ticketCode, int price, int availableSeats) {
            this.ticketCode = ticketCode;
            this.price = price;
            this.availableSeats = availableSeats;
        }

        public String getTicketCode() {
            return ticketCode;
        }

        public int getPrice() {
            return price;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        @Override
        public String toString() {
            return "Код Квитка = " + ticketCode + ", Ціна = " + price + " USD, Вільних місць = " + availableSeats;
        }
    }


    enum TransportationType {
        PLANE, TRAIN, BUS;
    }


    static void delay(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.err.println("Помилка під час очікування: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    static int getRandomDelay(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}