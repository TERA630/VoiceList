# テストアプリ
Recycler View　関連するアイテムへの遷移、アイテムの詳細表示
ViewModel＋LiveData使用

# コーディング規定
Activity,fragment,adapter : lifecycle , Event , サブクラス、インターフェイス、private methodの分類でABC順に関数は並べる
他のクラス　abc順に関数を並べる


# 役割規定
Activity Fragmentの管理､Menu Eventの管理
Fragment Fragmentの移動を伴うイベントの処理､Fragment Transition,
LiveDataの購読

ViewModel　Activity,Fragment､Adapterからデータ処理を伴うイベントの処理｡
          Viewへの参照は持たない｡

# レイアウトXML規定
最初にID
次にレイアウト関連の要素。Top　Start　End Bottomの順に規定
できるだけ親要素との関連のあるものから配置していく

# 調べたこと
## Recycler View関連
詳細のViewのExpand、Collapseを切り替える場合は､ViewHolder以外で開閉状態を保持。
LayoutにanimateLayoutChanges=trueをセットすると子View追加時にAnimationしてくれる。
サイズ変化はcontainerView?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)をすると　Animationしてくれる
## View関連
EditorView
背景を@android/tranparentとすると下線が消える｡
ViewAnimator
2つのViewを　ShowPrevious/ShowNextで切り替えられる
##Appbar layout
linerLayoutを継承　CoordinatorLayoutの直下にないと動かない。
兄弟となるScroliing view(scrolling sibling)に@string/appbar_scrolling_view_behaviorで
AppBarLayout.ScrollingViewBehaviorのbehavior classをセットしておく。
## 正規表現
0か1 ?
改行以外の任意の文字　.
改行を含む任意の文字　\S\s
## テスト
Espresso　AssertJ
recyclerView.findViewHolderAtPosition(positon)

カスタムMatcherはBoundedMathcer＜T Extend View>を使用｡
Matches SafetyはEspressoのフレームワークで､全ての＜T(TextViewやImageViewなど) Extend View＞を順次当てはめる｡
条件に当てはまればTrue､当てはまらなければ　Falseを返す｡
