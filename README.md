# コーディング規定
Activity,fragment,adapter : lifecycle , Event , サブクラス、インターフェイス、private methodの分類でABC順に関数は並べる
他のクラス　abc順に関数を並べる

#レイアウトXML規定
最初にID
次にレイアウト関連の要素。Top　Start　End Bottomの順に規定
できるだけ親要素との関連のあるものから配置していく

#調べたこと
##View関連
EditorView
背景を@android/tranparentとすると下線が消える｡

ViewAnimator
2つのViewを　ShowPrevious/ShowNextで切り替えられる
##テスト
Espresso　AssertJ
recyclerView.findViewHolderAtPosition(positon)

カスタムMatcherはBoundedMathcer＜T Extend View>を使用｡
Matches SafetyはEspressoのフレームワークで､全ての＜T(TextViewやImageViewなど) Extend View＞を順次当てはめる｡
条件に当てはまればTrue､当てはまらなければ　Falseを返す｡
