package org.example.jubensha.common;

import org.example.jubensha.entity.*;
import org.example.jubensha.mapper.GameMapper;
import org.example.jubensha.mapper.HistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ScriptDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ScriptDataInitializer.class);

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private HistoryMapper historyMapper;

    @PostConstruct
    public void init() {
        try {
            if (isDatabaseEmpty()) {
                logger.info("数据库为空，开始导入初始数据...");
                insertInitialData();
                logger.info("初始数据导入完成！");
            } else {
                logger.info("数据库已有数据，跳过初始数据导入");
            }
        } catch (Exception e) {
            logger.error("导入初始数据失败: {}", e.getMessage(), e);
        }
    }

    private boolean isDatabaseEmpty() {
        try {
            Integer count = gameMapper.countScripts();
            return count == null || count == 0;
        } catch (Exception e) {
            return true;
        }
    }

    private void insertInitialData() {
        logger.info("开始插入剧本系列数据...");
        insertSeries();
        
        logger.info("开始插入剧本数据...");
        insertScripts();
        
        logger.info("开始插入角色数据...");
        insertRoles();
        
        logger.info("开始插入幕次数据...");
        insertActs();
        
        logger.info("开始插入线索数据...");
        insertClues();
        
        logger.info("开始插入角色剧本内容...");
        insertRoleActContents();
        
        logger.info("开始插入结局数据...");
        insertEndings();
        
        logger.info("开始插入用户数据...");
        insertUsers();
    }

    private void insertSeries() {
        ScriptSeries series1 = new ScriptSeries();
        series1.setSeriesName("九卷寻踪");
        series1.setSeriesDesc("围绕《营造法式·山西抄本》残卷展开的系列剧本，融合山西古建营造技艺与悬疑推理。");
        gameMapper.insertSeries(series1);

        ScriptSeries series2 = new ScriptSeries();
        series2.setSeriesName("经典推理");
        series2.setSeriesDesc("经典推理剧本，适合新手入门。");
        gameMapper.insertSeries(series2);

        ScriptSeries series3 = new ScriptSeries();
        series3.setSeriesName("武侠江湖");
        series3.setSeriesDesc("武侠主题的推理剧本。");
        gameMapper.insertSeries(series3);
    }

    private void insertScripts() {
        gameMapper.insertScriptWithId(1, "九卷寻踪・营造终章", "九卷残卷集齐，守护队返回太原闭环收官。偷梁客头目终极现身，企图夺取全套残卷倒卖古建，众人以山西古建营造技艺为武器，破解最终机关，揭露偷梁客的真实身份与百年恩怨，完成古建守护使命，传承三晋营造文脉。", 3, "新手", "https://images.unsplash.com/photo-1592328726344-f901b389d623?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】偷梁客头目其实是守护队内部成员，他利用最后的机会企图独吞残卷。在大家的团结协作下，真相大白，残卷得到妥善保护。", 1, 1, 9, null);
        gameMapper.insertScriptWithId(11, "九卷寻踪・黄河古渡", "云冈石窟石刻被盗、崖壁暗窟被开启，第八卷残卷藏于石窟造像秘处。守护队解读北魏营造石刻铭文，对抗偷梁客的终极反扑，揭秘石窟与山西古建营造的传承脉络，最后一卷残卷线索回归太原。", 3, "进阶", "https://images.unsplash.com/photo-1518609434878-0607866d1a48?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】偷梁客利用石窟维修的掩护盗取文物，守护队通过分析营造铭文锁定嫌疑人，成功追回残卷。", 1, 1, 8, null);
        gameMapper.insertScriptWithId(12, "九卷寻踪・乔家深宅", "乔家大院砖雕被盗、祖宅暗格被撬，第七卷残卷藏于大院雕梁暗阁。守护队探查晋商顶级大院的营造巧思，破解家族秘辛与偷梁客的阴谋，揭开乔家与营造残卷的百年渊源，残卷线索指向云冈石窟。", 3, "进阶", "https://images.unsplash.com/photo-1543399068-8eb1adf480ce?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】乔家后人中的一员是偷梁客的内应，他试图通过盗取残卷来换取利益。守护队揭穿了阴谋，保护了文物。", 1, 1, 7, null);
        gameMapper.insertScriptWithId(13, "九卷寻踪・平遥城垣", "平遥古城墙砖石失窃、市楼机关被触发，第六卷残卷藏于古城中轴线秘处。守护队结合古城营造规制与晋商密网，揪出偷梁客在古城的残余势力，还原古城防御与金融共生的历史，残卷线索指向乔家大院。", 2, "进阶", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】古城守护者中出了叛徒，偷梁客利用古城的防御漏洞盗取残卷。守护队修复了防御系统并追回残卷。", 1, 1, 6, null);
        gameMapper.insertScriptWithId(15, "九卷寻踪・悬空古寺", "全国最高纯木古建应县木塔突发结构异动，塔内秘龛遭窃，第四卷残卷藏于塔心榫卯结构中。守护队凭借营造技艺拆解木塔机关，对抗偷梁客的破坏企图，揭秘木塔千年不倒的营造绝技，残卷线索引向悬空寺。", 4, "烧脑", "https://images.unsplash.com/photo-1626785774513-204d249a223c?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】偷梁客企图通过破坏木塔结构来盗取残卷，守护队利用榫卯结构知识识破了机关，成功保护了残卷和古建。", 1, 1, 5, null);
        gameMapper.insertScriptWithId(16, "九卷寻踪・应县木塔", "众人追至介休张壁古堡，探寻藏于地道中的第三卷残卷。古堡地道机关密布、军事暗哨重重，偷梁客设下陷阱企图夺卷，守护队破解地道榫卯防御，揭开古堡「军商合一」的营造秘辛，残卷线索指向应县木塔。", 3, "新手", "https://images.unsplash.com/photo-1549908217-db9480e1960d?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】古堡主人与偷梁客勾结，企图通过地道盗取残卷。守护队破解了复杂的机关，成功保护了文物。", 1, 1, 4, null);
        gameMapper.insertScriptWithId(17, "九卷寻踪・云冈秘刻", "守护队奔赴大同代王府，营救被软禁的营造匠人沈守义，追查第二卷残卷。暴雨夜王府机关异动，九龙壁琉璃缺失、藻井被撬，众人破解明代藩府营造机关，揪出王府内应，偷梁客头目现身逃亡，残卷线索指向张壁古堡。", 6, "烧脑", "https://images.unsplash.com/photo-1518432031301-314567c83377?q=80&w=800&auto=format&fit=crop", "原创制作", "【真相复盘】王府管家是偷梁客的内应，他软禁了沈守义并试图夺取残卷。守护队破解了王府机关，救出匠人，揪出内奸。", 1, 1, 3, null);
        
        gameMapper.insertScriptWithId(101, "暗夜豪门", "私人别墅举办晚宴，却在午夜时分离奇坠楼。监控显示没有外人进入，在场的只有三位客人，凶手就在你们之中...", 3, "新手", "https://images.unsplash.com/photo-1566753812264-135085244753?q=80&w=800&auto=format&fit=crop", "经典推理", null, 1, 1, null, null);
        gameMapper.insertScriptWithId(102, "云顶天宫", "武林盟主被发现死于密室之中，女娲石不翼而飞。各大门派齐聚云顶天宫，谁是真凶？", 4, "烧脑", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?q=80&w=800&auto=format&fit=crop", "武侠推理", null, 1, 1, null, null);
        gameMapper.insertScriptWithId(103, "古墓迷踪", "考古队在古墓中发现了神秘宝藏，随之而来的是一系列离奇死亡事件。谁在背后操纵一切？", 5, "进阶", "https://images.unsplash.com/photo-1511884642898-4c92249e20b6?q=80&w=800&auto=format&fit=crop", "探险推理", null, 1, 1, null, null);
        gameMapper.insertScriptWithId(104, "午夜学堂", "深夜的废弃学校传来诡异的声响，一个失踪多年的女学生似乎在呼唤着什么...", 4, "恐怖", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=800&auto=format&fit=crop", "惊悚推理", null, 1, 1, null, null);
        gameMapper.insertScriptWithId(105, "海上惊魂", "豪华游轮上的晚宴，一位富商突然死亡。在茫茫大海上，凶手就在乘客之中。", 6, "新手", "https://images.unsplash.com/photo-1517732306149-e8f829eb588a?q=80&w=800&auto=format&fit=crop", "经典推理", null, 1, 1, null, null);

        // 记录剧本发布历史
        recordScriptPublish(1, 1L, "九卷寻踪・营造终章");
        recordScriptPublish(11, 1L, "九卷寻踪・黄河古渡");
        recordScriptPublish(12, 1L, "九卷寻踪・乔家深宅");
        recordScriptPublish(13, 1L, "九卷寻踪・平遥城垣");
        recordScriptPublish(15, 1L, "九卷寻踪・悬空古寺");
        recordScriptPublish(16, 1L, "九卷寻踪・应县木塔");
        recordScriptPublish(17, 1L, "九卷寻踪・云冈秘刻");
        recordScriptPublish(101, 1L, "暗夜豪门");
        recordScriptPublish(102, 1L, "云顶天宫");
        recordScriptPublish(103, 1L, "古墓迷踪");
        recordScriptPublish(104, 1L, "午夜学堂");
        recordScriptPublish(105, 1L, "海上惊魂");
    }

    private void recordScriptPublish(Integer scriptId, Long userId, String title) {
        try {
            ScriptPublishHistory history = new ScriptPublishHistory();
            history.setScriptId(scriptId);
            history.setUserId(userId);
            history.setTitle(title);
            historyMapper.insertScriptPublish(history);
        } catch (Exception e) {
            logger.warn("记录剧本发布历史失败: scriptId={}, title={}", scriptId, title);
        }
    }

    private void insertRoles() {
        insertRole(1, "守护队长", 0, "你是守护队的核心成员，经验丰富，善于分析局势。你受邀参与终章任务。");
        insertRole(1, "营造大师", 1, "年过半百的老匠人，精通山西古建营造技艺，是守护队的重要成员。");
        insertRole(1, "文物专家", 1, "学识渊博的考古学家，对历史了如指掌，参与了残卷的解读工作。");
        
        insertRole(11, "石窟守护者", 0, "云冈石窟的守护者，熟悉石窟的每一寸结构。");
        insertRole(11, "文物贩子", 1, "可疑的陌生人，出现在石窟附近的可疑人物。");
        insertRole(11, "考古学生", 1, "周教授的学生，对石窟有深入研究。");
        
        insertRole(12, "乔家后人", 0, "乔家大院现在的继承人，对家族历史了如指掌。");
        insertRole(12, "老管家", 1, "在乔家工作了几十年的老管家。");
        insertRole(12, "神秘访客", 1, "自称是来参观的游客，但行为十分可疑。");
        
        insertRole(13, "古城守望者", 0, "平遥古城的守护者，对城墙结构非常熟悉。");
        insertRole(13, "晋商后裔", 1, "晋商家族的后代，了解古城的金融秘密。");
        
        insertRole(15, "建筑学者", 0, "专攻古建筑研究的学者，对悬空寺的营造技艺有深入研究。");
        insertRole(15, "工程师", 1, "精通结构力学的工程师，能分析古建的结构原理。");
        insertRole(15, "历史学家", 1, "对历史了如指掌的教授，了解悬空寺的传说。");
        insertRole(15, "探险家", 1, "经验丰富的冒险者，曾多次深入古建考察。");
        
        insertRole(16, "考古队长", 0, "经验丰富的考古队负责人，带领团队追查残卷线索。");
        insertRole(16, "地道专家", 1, "精通地下工程的专家，擅长破解地道机关。");
        insertRole(16, "历史研究员", 1, "研究张壁古堡历史多年的学者。");
        
        insertRole(17, "赵清越", 0, "女，28岁，山西博物院文物修复师，赵氏后人，世代守护晋阳文物。");
        insertRole(17, "魏寻", 1, "男，30岁，考古学博士，周敬之教授的得意门生，理性严谨，逻辑思维极强。");
        insertRole(17, "韩墨", 1, "男，26岁，山西省公安厅刑警，文物犯罪侦查专家，行动敏捷，观察力敏锐。");
        insertRole(17, "智瑶", 1, "女，27岁，海外流失文物调查员，智氏后人，精通多国语言，性格飒爽果决。");
        insertRole(17, "苏瑾年", 1, "女，24岁，山西博物院策展人，特展总策划，性格开朗外向。");
        insertRole(17, "范长庚", 1, "男，52岁，民间文保专家，范氏守墓人后代，沉稳老练。");
        
        insertRole(101, "大少爷", 0, "老爷的大儿子，脾气暴躁，昨晚因遗产问题和老爷大吵一架。");
        insertRole(101, "女仆长", 1, "在洋馆工作十年的老仆人，暗地里欠下巨额赌债。");
        insertRole(101, "管家", 1, "看着少爷长大，忠心耿耿，掌握着洋馆最不可告人的秘密。");
        
        insertRole(102, "剑宗大弟子", 0, "名门正派的杰出弟子，剑法超群，对武林盟主忠心耿耿。");
        insertRole(102, "魔教圣女", 1, "神秘的魔教传人，行踪诡秘，武功高强。");
        insertRole(102, "药王谷主", 1, "精通医术和毒术的世外高人，性格古怪。");
        insertRole(102, "丐帮长老", 1, "消息灵通的丐帮头目，掌握很多江湖秘密。");
        
        insertRole(103, "考古队长", 0, "经验丰富的考古专家，带领团队深入古墓。");
        insertRole(103, "富二代", 1, "资助考古项目的富商之子，目的不纯。");
        insertRole(103, "盗墓者", 1, "隐藏在考古队中的盗墓贼。");
        insertRole(103, "神秘女子", 1, "古墓守护者的后裔，知道古墓的秘密。");
        insertRole(103, "地质学家", 1, "研究地质结构的专家。");
        
        insertRole(104, "转学生", 0, "刚转入学校的新生，似乎能看到别人看不到的东西。");
        insertRole(104, "学校老师", 1, "在学校工作多年的教师，了解学校的过去。");
        insertRole(104, "校工", 1, "负责学校杂务的员工，经常在深夜巡逻。");
        insertRole(104, "女学生", 1, "失踪女学生的闺蜜，试图寻找真相。");
        
        insertRole(105, "富商秘书", 0, "富商的私人秘书，掌握很多商业机密。");
        insertRole(105, "船医", 1, "游轮上的医生，负责乘客的健康。");
        insertRole(105, "赌王", 1, "在船上开设赌局的神秘人物。");
        insertRole(105, "女明星", 1, "受邀参加晚宴的知名演员。");
        insertRole(105, "富商妻子", 1, "富商的妻子，似乎有外遇。");
        insertRole(105, "侦探", 1, "受雇于富商的私人侦探。");
    }

    private void insertRole(int scriptId, String name, int isAi, String background) {
        Role role = new Role();
        role.setScriptId(scriptId);
        role.setName(name);
        role.setIsAi(isAi);
        role.setBackground(background);
        gameMapper.insertRole(role);
    }

    private void insertActs() {
        insertAct(1, "第一幕：终章开启", 1, "九卷残卷终于集齐，守护队返回太原准备开启最后的解密。王府内气氛紧张，机关重重...");
        insertAct(1, "第二幕：终极对决", 2, "偷梁客头目现身，一场关于古建守护的终极对决即将展开...");
        insertAct(1, "终幕：传承", 3, "真相揭开，古建守护的使命将继续传承。");
        
        insertAct(11, "第一幕：石窟惊变", 1, "云冈石窟发生盗窃案，守护队赶到现场展开调查...");
        insertAct(11, "第二幕：暗窟追踪", 2, "线索指向崖壁暗窟，守护队深入调查...");
        
        insertAct(13, "第一幕：古城疑云", 1, "平遥古城发生多起盗窃案，守护队介入调查...");
        insertAct(13, "第二幕：市楼机关", 2, "线索指向市楼的隐藏机关...");
        
        insertAct(15, "第一幕：木塔异动", 1, "应县木塔突然发出奇怪的声响，众人赶到现场...");
        insertAct(15, "第二幕：榫卯玄机", 2, "发现木塔内部藏有机关，需要破解榫卯结构...");
        insertAct(15, "第三幕：守护真相", 3, "成功保护残卷，揭开木塔千年不倒的秘密。");
        
        insertAct(16, "第一幕：古堡迷踪", 1, "守护队来到张壁古堡，发现地道入口...");
        insertAct(16, "第二幕：地道机关", 2, "深入地道，破解复杂的机关...");
        insertAct(16, "第三幕：军商秘辛", 3, "揭开古堡军商合一的历史秘密。");
        
        insertAct(17, "第一幕：博物馆惊魂", 1, "2025年深秋，山西博物院特展前夜，青铜戈残片的秘密即将揭开...");
        insertAct(17, "第二幕：密室凶案", 2, "周教授被发现死于密室之中，青铜戈仿制品成为凶器...");
        insertAct(17, "第三幕：双时空真相", 3, "跨越两千五百年的时空交错，晋阳之战的真相浮出水面...");
        
        insertAct(101, "第一幕：血染的书房", 1, "老爷被发现倒在书房之中，现场是一个完美的密室...");
        insertAct(101, "第二幕：隐藏的杀机", 2, "每个人都有秘密，真相究竟是什么...");
        insertAct(101, "终幕：真相大白", 3, "凶手被揭露，正义得到伸张。");
        
        insertAct(102, "第一幕：云顶疑云", 1, "武林盟主离奇死亡，女娲石失踪，各大门派齐聚云顶天宫...");
        insertAct(102, "第二幕：暗流涌动", 2, "各派之间矛盾重重，凶手就在你们之中...");
        insertAct(102, "终幕：江湖真相", 3, "真相大白，凶手伏法。");
        
        insertAct(103, "第一幕：古墓开启", 1, "考古队打开尘封已久的古墓...");
        insertAct(103, "第二幕：死亡诅咒", 2, "接连发生离奇死亡事件...");
        insertAct(103, "终幕：守护使命", 3, "揭开古墓秘密，保护文物。");
        
        insertAct(104, "第一幕：深夜声响", 1, "废弃学校深夜传来诡异声响...");
        insertAct(104, "第二幕：亡魂呼唤", 2, "失踪女学生的灵魂似乎在呼唤...");
        insertAct(104, "终幕：超度安息", 3, "揭开真相，帮助亡魂安息。");
        
        insertAct(105, "第一幕：海上晚宴", 1, "豪华游轮上的晚宴，富商突然死亡...");
        insertAct(105, "第二幕：嫌疑重重", 2, "每个人都有嫌疑，真相扑朔迷离...");
        insertAct(105, "终幕：深海谜团", 3, "凶手被揭露，但背后似乎还有更大的阴谋。");
    }

    private void insertAct(int scriptId, String actName, int sort, String publicContent) {
        Act act = new Act();
        act.setScriptId(scriptId);
        act.setActName(actName);
        act.setSort(sort);
        act.setPublicContent(publicContent);
        gameMapper.insertAct(act);
    }

    private void insertClues() {
        gameMapper.insertClue(1, "神秘残卷", "记载着古建营造技艺的残卷，上面有看不懂的符号。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(1, "机关图纸", "一张古老的机关图纸，标注着复杂的榫卯结构。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(1, "可疑脚印", "现场发现的一串脚印，指向地道方向。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(11, "被盗石刻", "云冈石窟被盗的北魏石刻，上面有营造铭文。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(11, "维修记录", "石窟维修的施工记录，有可疑的修改痕迹。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(15, "松动榫卯", "木塔某处的榫卯结构被人为松动。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(15, "神秘符号", "塔心发现的古代符号，似乎是某种密码。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(15, "工匠笔记", "记录了木塔营造技艺的工匠笔记。", 1, null, 3, 0, null, null);
        
        gameMapper.insertClue(16, "地道地图", "标注着古堡地道分布的手绘地图。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(16, "古代兵器", "在地道深处发现的古代军事器械。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(17, "青铜戈残片", "展厅里的核心展品，内侧有被刮掉的铭文。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(17, "周教授的字条", "「戈分两半，史藏真章，晋阳之秘，不可示人」", 1, null, 1, 0, null, null);
        gameMapper.insertClue(17, "密室现场", "办公区门窗均从内部锁死，是完美的密室。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(17, "青铜戈仿制品", "凶案现场的凶器，仿制工艺精良。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(17, "修复工具", "文物修复师的专用工具出现在现场。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(101, "带血的烟灰缸", "书房里发现的可疑物品，上面有血迹。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(101, "安眠药瓶", "女仆房间里发现的药瓶，里面还有剩余药物。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(101, "雪茄烟头", "管家常抽的雪茄烟头，出现在走廊。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(101, "毒药样本", "后花园种植的有毒植物样本。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(102, "破碎玉佩", "现场发现的破碎玉佩，是打斗的痕迹。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(102, "奇怪药粉", "一种罕见的毒药，只有药王谷的人才懂。", 1, null, 2, 0, null, null);
        gameMapper.insertClue(102, "女娲石碎片", "在密室角落发现的宝石碎片。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(103, "古墓地图", "标注古墓内部结构的地图。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(103, "诅咒石碑", "刻有神秘诅咒文字的石碑。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(104, "褪色照片", "多年前学校全貌的照片。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(104, "失踪档案", "记录失踪女学生信息的档案。", 1, null, 2, 0, null, null);
        
        gameMapper.insertClue(105, "富商遗嘱", "富商留下的遗嘱，指定了遗产分配。", 1, null, 1, 0, null, null);
        gameMapper.insertClue(105, "可疑情书", "富商妻子与不明男子的通信。", 1, null, 2, 0, null, null);
    }

    private void insertRoleActContents() {
        insertRoleActContent(1, 1, getRoleIdByName(1, "守护队长"), "作为守护队核心成员，你召集大家来到太原，准备开启终章任务。残卷上的线索指向最后一个目标。");
        insertRoleActContent(1, 2, getRoleIdByName(1, "守护队长"), "偷梁客头目终于现身，你们必须在他之前找到残卷并揭开真相。");
        
        int role171Id = getRoleIdByName(17, "赵清越");
        insertRoleActContent(17, 1, role171Id, "你是赵氏后人，这枚青铜戈是你祖上亲手铸造的守城信物。你比任何人都清楚，这枚戈从来都不是兵器，而是晋阳城的守城「量天尺」。");
        insertRoleActContent(17, 2, role171Id, "你是文物修复师，对青铜材质的器物了如指掌。你发现凶案现场的仿制品上有你修复工具留下的痕迹。");
        
        int role201Id = getRoleIdByName(101, "大少爷");
        int role202Id = getRoleIdByName(101, "女仆长");
        int role203Id = getRoleIdByName(101, "管家");
        
        insertRoleActContent(101, 1, role201Id, "昨晚10点你去找过老头子，但他把你骂出来了，你气得在房间喝酒到天亮。你没杀他，但你看到女仆长端着一杯红茶在走廊鬼鬼祟祟。");
        insertRoleActContent(101, 2, role201Id, "其实你昨晚11点半又去了一次书房，发现门虚掩着，老头子倒在地上。你为了不被怀疑，偷偷拿走了桌上的凶器——一个带血的烟灰缸，藏在了自己床下。");
        
        insertRoleActContent(101, 1, role202Id, "昨晚10点半你给老爷端了红茶，里面放了安眠药，因为你想去他书房偷点古董还债。但你进去时他已经死了！你吓得打碎了茶杯赶紧跑了。");
        insertRoleActContent(101, 2, role202Id, "你忽然想起来，昨晚在走廊不仅看到了大少爷，还闻到了一股特殊的雪茄味，那是管家常抽的牌子。");
        
        insertRoleActContent(101, 1, role203Id, "昨晚11点你听到书房有动静，过去一看门是锁着的，你从钥匙孔看到一个高大的黑影，很像大少爷的身形。");
        insertRoleActContent(101, 2, role203Id, "警方发现的毒药让你很慌，因为那种毒药（乌头草）就种在后花园，而整个洋馆只有你负责打理花园。你决定把脏水泼给女仆长。");
        
        int role301Id = getRoleIdByName(102, "剑宗大弟子");
        int role302Id = getRoleIdByName(102, "魔教圣女");
        int role303Id = getRoleIdByName(102, "药王谷主");
        int role304Id = getRoleIdByName(102, "丐帮长老");
        
        insertRoleActContent(102, 1, role301Id, "你一直对武林盟主忠心耿耿，但他最近似乎对你有所猜忌。昨晚你一直在自己的房间修炼剑法。");
        insertRoleActContent(102, 2, role302Id, "你受邀参加这次聚会其实另有目的，女娲石是魔教必得之物。");
        insertRoleActContent(102, 1, role303Id, "你精通医术和毒术，但你昨晚一直在研制解药，因为你知道自己可能中毒了。");
        insertRoleActContent(102, 2, role304Id, "你掌握着很多江湖秘密，但你没想到这次会牵扯出这么大的阴谋。");
    }

    private void insertRoleActContent(int scriptId, int actId, int roleId, String content) {
        RoleActContent rac = new RoleActContent();
        rac.setScriptId(scriptId);
        rac.setActId(actId);
        rac.setRoleId(roleId);
        rac.setContent(content);
        gameMapper.insertRoleActContent(rac);
    }

    private void insertEndings() {
        int role201Id = getRoleIdByName(101, "大少爷");
        int role202Id = getRoleIdByName(101, "女仆长");
        int role203Id = getRoleIdByName(101, "管家");
        
        insertEnding(101, role201Id, "大少爷伏法", "大少爷承认了罪行，他为了争夺遗产用钝器杀害了老爷。他拿走的凶器成为关键证据。");
        insertEnding(101, role202Id, "女仆长伏法", "女仆长承认了罪行，她为了还债在红茶里下了安眠药，导致老爷心脏病发作。");
        insertEnding(101, role203Id, "管家伏法", "管家才是真正的凶手，他用乌头草毒杀了老爷，企图掩盖更大的秘密。");
        
        int role301Id = getRoleIdByName(102, "剑宗大弟子");
        int role302Id = getRoleIdByName(102, "魔教圣女");
        int role303Id = getRoleIdByName(102, "药王谷主");
        
        insertEnding(102, role301Id, "冤案昭雪", "剑宗大弟子被冤枉，真凶其实是魔教圣女。她用毒术控制了整个局面，企图夺取女娲石。");
        insertEnding(102, role302Id, "圣女伏法", "魔教圣女的阴谋被揭穿，她被当场擒获。女娲石完璧归赵。");
        insertEnding(102, role303Id, "药王中计", "药王谷主其实是受害者，他被人陷害。真正的幕后黑手另有其人。");
        
        insertEnding(103, null, "宝藏守护", "考古队发现古墓中的宝藏是古代文物，决定全部上交国家。");
        insertEnding(104, null, "灵魂安息", "失踪女学生的心愿得到完成，她的灵魂终于安息。");
        insertEnding(105, null, "海上审判", "凶手在铁证面前认罪伏法，正义得到伸张。");
    }

    private void insertEnding(int scriptId, Integer votedRoleId, String endingTitle, String endingContent) {
        ScriptEnding ending = new ScriptEnding();
        ending.setScriptId(scriptId);
        ending.setVotedRoleId(votedRoleId);
        ending.setEndingTitle(endingTitle);
        ending.setEndingContent(endingContent);
        gameMapper.insertScriptEnding(ending);
    }

    private void insertUsers() {
        try {
            // userId=1 留给 admin，从 100 开始
            insertUser(100L, "剧遇小萌新", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138001", 0, "欢乐", 2, "JY000001", "", "", null, "上海", "在波士顿的人", null);
            insertUser(101L, "剧本推土机", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138002", 1, "阵营机制", 5, "JY000002", null, null, null, null, null, null);
            insertUser(102L, "情感水龙头", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138003", 2, "情感沉浸", 3, "JY000003", null, null, null, null, null, null);
            insertUser(103L, "恐怖坦克", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138004", 1, "恐怖惊悚", 4, "JY000004", null, null, null, null, null, null);
            insertUser(104L, "欢乐喜剧人", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138005", 2, "欢乐搞笑", 2, "JY000005", null, null, null, null, null, null);
            insertUser(105L, "硬核侦探", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138006", 1, "硬核推理", 6, "JY000006", null, null, null, null, null, null);
            insertUser(106L, "古装爱好者", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138007", 2, "古风情感", 3, "JY000007", null, null, null, null, null, null);
            insertUser(107L, "机制老骗子", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138008", 1, "阵营机制", 5, "JY000008", null, null, null, null, null, null);
            insertUser(108L, "新手小白", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E", "13800138009", 0, "未设置", 1, "JY000009", null, null, null, null, null, null);
            insertUser(109L, "全能玩家", "cj206716", "13800138010", 2, "全类型通吃", 7, "JY000010", null, null, null, null, null, null);
            insertUser(110L, "cj06716", "$2a$10$RUzK5ve21eTJsnk7WeyWaOwaajBckaBgetYRhXVkEbD3mK2l/HbiW", "16636862273", 1, "推理", 7, "A5BBFE7C", "cjyyds", "李成杰", null, "上海", "一个脑洞达人,多次在推理中解救同伴于危难之中", "/images/avatar_1771915686380_60dfede0789e6e876c73b9d77b1b54c8.webp");
            insertUser(111L, "cj0000", "$2a$10$whM.tyMJO36JYiA4fe1p8eloPXhbt74Wn7y0rQHQR71aGv.d2m2/q", "16636862274", 0, "情感", 1, "A1772889043862", "cj0000", "陈雅茹", null, "上海", null, "/uploads/avatar_1774698196751_4ac6d67e36e2092476ccad07cb135d08.jpg");
            logger.info("真实用户数据导入成功");
        } catch (Exception e) {
            logger.warn("用户数据已存在或导入失败: {}", e.getMessage());
        }
    }

    private void insertUser(Long userId, String username, String password, String phone, Integer gender, 
                           String hobbyType, Integer userLevel, String uid, String nickname, 
                           String realName, java.time.LocalDate birthday, String city, String profile, String avatarUrl) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setGender(gender);
        user.setHobbyType(hobbyType);
        user.setUserLevel(userLevel);
        user.setUid(uid);
        user.setNickname(nickname);
        user.setRealName(realName);
        user.setBirthday(birthday);
        user.setCity(city);
        user.setProfile(profile);
        user.setAvatarUrl(avatarUrl);
        gameMapper.insertUser(user);
    }

    private int getRoleIdByName(int scriptId, String roleName) {
        Integer id = gameMapper.getRoleIdByName(scriptId, roleName);
        return id != null ? id : 0;
    }
}