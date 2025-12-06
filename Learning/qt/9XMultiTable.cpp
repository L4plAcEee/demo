#include <QApplication>
#include <QMainWindow>
#include <QDebug>
#include <QLabel>
#include <QVBoxLayout>
#include <QGridLayout>
// QObject 作为 所有Qt对象的基类，可以通过对象树的设计连环 Delete子对象

class Label : public QLabel {
public:
    // explicit 关键字是 C++ 中用于 构造函数和转换运算符 的一个修饰符，
    // 它的主要作用是防止 隐式类型转换（implicit conversion），从而提高类型安全性，避免潜在的错误。
    explicit Label(QWidget *parent = nullptr) : QLabel(parent) {
        setText("这是一个标签的默认文本O。o");
        setAlignment(Qt::AlignCenter);
        setStyleSheet(R"(
        QLabel {
            color: #333333;
            background-color: qlineargradient(
                spread:pad, x1:0, y1:0, x2:1, y2:1,
                stop:0 #f0f0f0, stop:1 #dcdcdc
            );
            border: 1px solid #b0b0b0;
            border-radius: 10px;
            padding: 10px;
            font-size: 16px;
            font-weight: bold;
            font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
        }
    )");
    }
};

class MainWindow : public QMainWindow {
public:
    explicit MainWindow(QWidget *parent = nullptr) :
    QMainWindow(parent) {

        // 怎么知道的 QApplication? 答案在于创建 QMainWindow对象 的时候，
        // 调用了 QApplication::instance() -> m_queue(事件队列) -> close -> exit()

        setGeometry(0, 0, 900, 600);
        setWindowTitle("难绷之第一个QT程序：九九乘法表");

        QWidget* centralWidget = new QWidget(this);
        setCentralWidget(centralWidget);

        // QVBoxLayout* layout = new QVBoxLayout(this);
        QGridLayout* layout = new QGridLayout(centralWidget);
        centralWidget->setLayout(layout);

        for (int i = 1; i <= 9; ++i) {
            for (int j = 1; j <= 9; ++j) {
                Label* label = new Label(this);
                label->setText(QString("%1 乘 %2 等于 %3").arg(i).arg(j).arg(i * j));
                layout->addWidget(label, i, j);
            }
        }


        // Label* label1 = new Label(this);
        // label1->setText("这是 Label1 喵");
        // layout->addWidget(label1);
        //
        // Label* label2 = new Label(this);
        // label2->setText("这是 Label2 喵");
        // layout->addWidget(label2);

        // for (int i = 1; i <= 3; ++i) {
        //     Label* label = new Label(this);
        //     // label->setText("这是 Label" + QString::number(i) + " 喵");
        //     label->setText(QString("%1 的平方等于 %2").arg(i).arg(i * i));
        //     layout->addWidget(label);
        // }

        qDebug() << "Is window hidden: "  << isHidden();
        show();
        qDebug() << "Is Window hidden: "  << isHidden();
    }
};


int main(int argc, char **argv) {
    // *必须要有命令行参数，不然会构造失败* -> 创建了一个单例
    QApplication app(argc, argv);
    MainWindow w;
    // 开启事件循环，用Qt事件循环替代主循环
    return app.exec();
}