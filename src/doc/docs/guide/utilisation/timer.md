# Timer
## Ajout du widget Timer à OBS
Dans OBS, ajoutez une source Navigateur.  
![img.png](timer_img/img.png)
![img_1.png](timer_img/img_1.png)
  
Saisissez l’URL en définissant la durée du Timer en secondes : `https://localhost:8443/timer/widget?time=[temps-en-s]`

Les dimensions doivent être supérieur à 500x500. Le fond étant transparent, vous pourrez placer le widget facilement sur
vos scènes.

Par exemple pour un timer de 5s :  
![img_2.png](timer_img/img_2.png)
 
Plus bas, cochez la case _Rafraîchir le navigateur lorsque la scène devient active_ pour pouvoir réarmer le Timer au 
prochain affichage.  
![img_3.png](timer_img/img_3.png)
 
À l’affichage du widget, le Timer commence. Il disparaîtra automatiquement une fois que le temps est écoulé.  
![img_4.png](timer_img/img_4.png)
 
