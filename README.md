This project is a telegram bot(@ZonkGamebot) for playing Zonk in group chats.
Zonk is a dice game with the goal to reach winning score(10,000 points) before other players. 

Bot commands

/start@ZonkGamebot - start a new game
/surrender@ZonkGamebot - exit the game
For chat admins only:
/reset@ZonkGamebot - reset the game
/kick@ZonkGamebot @username - kick player @username from the game 

Rules

For the game you need at least two players.
The player begins turn with a roll of 6 dice.
Then player must choose the combinations that will bring points.
Then player can either end the turn and save points, or repeat the throw with the remaining dice.
If all the dice are collected, then the number of available dice becomes 6 again.
If during the throw no combination brings the points, then all points for this turn are burned, and the move goes to the next player.
The game ends when one of the players reaches 10,000 points.

Combinations

Straight - six different - 2000 points
Three pairs - 1500 points
Three of a kind - value * 100 (Except three of Ones that brings - 1000)
The same starting from the fourth multiply the result by 2 (three fours - 400, four fours - 800, five fours - 1600)
One - 100
Five - 50
