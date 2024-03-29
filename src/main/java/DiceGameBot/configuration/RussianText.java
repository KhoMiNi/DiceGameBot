package DiceGameBot.configuration;

public class RussianText {
    public static final String RULES = "Для игры нужно минимум два участника.\n" +
            "Свой ход игрок начинает с броска 6 костей. \n" +
            "Затем он должен выбрать комбинации, которые принесут ему очки.\n" +
            "После чего можно либо закончить ход и сохранить очки, либо повторить бросок оставшимися костями.\n" +
            "Если собраны все кости, то количество доступных костей снова становится 6.\n" +
            "Если во время броска не выпадает ни одной приносящей очки комбинации, то все очки за этот ход сгорают, а ход переходит к следующему игроку.\n" +
            "Игра завершается, когда один из игроков достиг 10000 очков.\n\n" +
            "Комбинации:\n" +
            "Стрейт - шесть разных - 2000 очков\n" +
            "Три пары - 1500 очков\n" +
            "Три одинаковых - значение*100 \n" +
            "Три единицы - 1000\n" +
            "Одинаковые начиная с четвертой умножают результат на 2(три тройки - 300, четыре тройки - 600, пять троек - 1200)  \n" +
            "Единица - 100\n" +
            "Пятерка - 50\n\n" +
            "- команда /start@ZonkGamebot - начать новую игру\n" +
            "- команда /surrender@ZonkGamebot - выйти из игры\n" +
            "Только для админов чата:\n" +
            "- команда /reset@ZonkGamebot - сбросить игру\n" +
            "- команда /kick@ZonkGamebot @username - исключить из игры игрока @username";;

}
