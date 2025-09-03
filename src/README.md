\# Dolatip



A custom \*\*animated tooltip\*\* for Java Swing.  

It provides a modern look with fade-in/out animations, expandable transaction history, and smooth resizing.  



\## ✨ Features

\- 🪄 Fade-in / fade-out animations  

\- 📈 Trend indicators (▲ ▼ ●) for numeric values  

\- 📜 Expandable view when data exceeds a threshold  

\- 🎨 Customizable styles via `Config` (colors, width, font, etc.)  

\- 🖼 Rounded corners \& shadow effect  



\## 📸 Demo



Light usage:

!\[Dolatip small](demo-small.gif)



Expanded:

!\[Dolatip expanded](demo-expanded.gif)



\*(Replace with your actual GIF filenames in the repo)\*  



\## ⚡ Usage



```java

import javax.swing.\*;

import java.util.Arrays;



public class Main {

&nbsp;   public static void main(String\[] args) {

&nbsp;       JFrame frame = new JFrame("Dolatip Demo");

&nbsp;       frame.setDefaultCloseOperation(JFrame.EXIT\_ON\_CLOSE);

&nbsp;       JButton button = new JButton("Hover me!");

&nbsp;       frame.add(button);

&nbsp;       frame.setSize(300, 200);

&nbsp;       frame.setVisible(true);



&nbsp;       Dolatip.Config config = new Dolatip.Config("Transactions")

&nbsp;               .width(200)

&nbsp;               .maxBeforeClick(3)

&nbsp;               .maxAfterClick(8);



&nbsp;       Dolatip.attachTo(button, Arrays.asList(10.0, 12.5, 11.8, 14.3), config);

&nbsp;   }

}

⚙️ Config Options

Option	Description	Default

backgroundColor	Tooltip background color	Dark gray

width(int)	Tooltip width in px	160

maxBeforeClick	Items shown before click (collapsed view)	2

maxAfterClick	Items shown after click (expanded view)	6

cornerRadius	Rounded corner radius	8

offset(x, y)	Tooltip offset from parent component	(5,0)

transparency	Background transparency percentage (0–100)	0



📦 Installation

Just copy Dolatip.java into your project under a suitable package, e.g.:



css

Copy code

src/Main/themes/Dolatip.java

📝 License

MIT License – feel free to use and modify.

