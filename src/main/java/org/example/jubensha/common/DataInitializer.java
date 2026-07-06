package org.example.jubensha.common;

import jakarta.annotation.PostConstruct;
import org.example.jubensha.entity.*;
import org.example.jubensha.mapper.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private GameMapper gameMapper;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void initTestData() {
        try {
            // 初始化默认用户
            if (gameMapper.getUserCount() == 0) {
                createDefaultUser();
                System.out.println("=== 已初始化默认用户 ===");
            }
            
            // 初始化剧本数据 - 已禁用，使用数据库手动导入
            // if (gameMapper.getScriptList().isEmpty()) {
            //     createTestScripts();
            //     System.out.println("=== 已初始化测试剧本数据 ===");
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建默认用户
     */
    private void createDefaultUser() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("暗区兵王");
        admin.setPhone("13800138000");
        admin.setGender(1);
        admin.setHobbyType("推理,剧本杀");
        admin.setUserLevel(10);
        admin.setUid("admin_uid_001");
        admin.setRealName("管理员");
        admin.setCity("北京");
        admin.setProfile("我是暗区兵王，欢迎大家！");
        admin.setUserId(1L);
        gameMapper.insertUser(admin);
    }

    private void createTestScripts() {
        Script script1 = new Script();
        script1.setTitle("孤岛惊魂");
        script1.setIntro("你们在海上遭遇风暴，流落到这座与世隔绝的孤岛。当第一具尸体出现时，大家都明白，凶手就在你们之中...");
        script1.setPlayerCount(6);
        script1.setDifficulty("5");
        script1.setCoverUrl(null);
        script1.setTags("推理,硬核,密室");
        script1.setTruthContent("凶手是管家，他为了报复十年前主人抢走他未婚妻的仇恨，精心策划了这起连环谋杀案。他利用密室机关制造不在场证明，通过伪造的航海日志误导大家的判断。");
        script1.setUserId(1L);
        gameMapper.insertScript(script1);
        createScript1Components(script1.getScriptId());

        Script script2 = new Script();
        script2.setTitle("青山入云间");
        script2.setIntro("天禧二十三年，大楚皇城内风云暗涌。一首绝命诗，牵扯出十年前的旧案。谁是执棋者，谁又是局中人？");
        script2.setPlayerCount(6);
        script2.setDifficulty("3");
        script2.setCoverUrl(null);
        script2.setTags("古风,情感,演绎");
        script2.setTruthContent("真凶是当朝宰相，他为了掩盖自己通敌叛国的秘密，杀害了发现真相的太子太傅。那首绝命诗是太傅留下的密码，指向宰相府密室中的密信。");
        script2.setUserId(1L);
        gameMapper.insertScript(script2);
        createScript2Components(script2.getScriptId());

        Script script3 = new Script();
        script3.setTitle("拆迁暴发户");
        script3.setIntro("老城坊终于要拆迁了！为了争夺巨额拆迁款，老王家这七口人可谓是八仙过海各显神通，爆笑互撕即将开始！");
        script3.setPlayerCount(7);
        script3.setDifficulty("1");
        script3.setCoverUrl(null);
        script3.setTags("欢乐,机制,撕逼");
        script3.setTruthContent("假装失踪的老王头其实一直躲在阁楼里，他想看看孩子们为了钱会如何表现。最后被最孝顺的小女儿发现，一家人终于明白亲情比金钱更重要。");
        script3.setUserId(1L);
        gameMapper.insertScript(script3);
        createScript3Components(script3.getScriptId());

        Script script4 = new Script();
        script4.setTitle("山村老尸");
        script4.setIntro("偏僻的山村，诡异的童谣。听说后山的破庙里，每到半夜就会传出女人的哭声。你们作为探险主播，决定一探究竟...");
        script4.setPlayerCount(5);
        script4.setDifficulty("3");
        script4.setCoverUrl(null);
        script4.setTags("恐怖,民俗,变格");
        script4.setTruthContent("所谓的女鬼其实是村长的女儿，她为了给被村民迫害致死的母亲报仇，利用村民的迷信心理策划了一系列恐怖事件。那首童谣是她母亲生前教她的催眠曲。");
        script4.setUserId(1L);
        gameMapper.insertScript(script4);
        createScript4Components(script4.getScriptId());

        Script script5 = new Script();
        script5.setTitle("代码杀机");
        script5.setIntro("距离产品上线仅剩几小时，核心服务器惨遭 rm -rf /*！大门反锁，5名互联网打工人互踩。是谁删库跑路？");
        script5.setPlayerCount(5);
        script5.setDifficulty("3");
        script5.setCoverUrl(null);
        script5.setTags("程序员,职场,欢乐");
        script5.setTruthContent("删库的是新来的实习生，他误执行了测试环境的清理脚本。但真正想搞破坏的是产品经理，他修改了脚本路径指向生产环境，想借此机会推翻整个项目。");
        script5.setUserId(1L);
        gameMapper.insertScript(script5);
        createScript5Components(script5.getScriptId());

        Script script6 = new Script();
        script6.setTitle("暗夜钟声");
        script6.setIntro("欧洲中世纪古堡，十二点的钟声敲响，伯爵死在了密室中。完美的犯罪手法，这究竟是幽灵作祟还是人为？");
        script6.setPlayerCount(7);
        script6.setDifficulty("5");
        script6.setCoverUrl(null);
        script6.setTags("欧式,硬核,还原");
        script6.setTruthContent("凶手是伯爵的私人医生，他利用特制的时钟机关在钟声响起时射出毒针。密室手法是利用冰融化后的水让门锁自动落下，制造完美犯罪的假象。");
        script6.setUserId(1L);
        gameMapper.insertScript(script6);
        createScript6Components(script6.getScriptId());
    }

    private void createScript1Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("船长"); r1.setBackground("经验丰富的老船长，熟悉这片海域的每一处暗礁。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("女乘客"); r2.setBackground("神秘的富家千金，独自乘船旅行。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("管家"); r3.setBackground("跟随主人多年的老管家，忠心耿耿。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("水手"); r4.setBackground("年轻力壮的水手，对船长非常崇拜。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("厨师"); r5.setBackground("沉默寡言的厨师，总是独自待在厨房。"); gameMapper.insertRole(r5);
        Role r6 = new Role(); r6.setScriptId(scriptId); r6.setName("医生"); r6.setBackground("随船医生，医术高明但性格古怪。"); gameMapper.insertRole(r6);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：风暴来袭"); act1.setSort(1); act1.setPublicContent("暴风雨中，你们乘坐的客轮触礁沉没，幸运的是，大家都被海浪冲到了一座荒岛上..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：第一具尸体"); act2.setSort(2); act2.setPublicContent("第二天清晨，船长被发现死在了海滩上，胸口插着一把匕首..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：真相大白"); act3.setSort(3); act3.setPublicContent("幸存者们聚集在一起，必须找出隐藏在你们之中的凶手..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("作为船长，我比任何人都了解这片海域。昨晚的风暴虽然猛烈，但我注意到海浪的方向有些异常，似乎有人在刻意引导船只触礁。更让我不安的是，在出发前我收到过一封匿名恐吓信..."); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("我独自旅行是为了寻找我的未婚夫，他乘坐的上一艘船也在同一片海域失事了。这次风暴让我觉得有人在暗中操控一切，那封我收到的神秘信件似乎在警告我远离这座岛..."); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("我跟随主人在海上漂泊了二十年，这次遇险绝非偶然。我注意到船长最近行为异常，似乎在隐瞒什么。而且我在风暴前夕看见甲板上出现过一个黑影..."); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("我崇拜船长，但昨晚我看到了一些不该看的东西。风暴中，船长似乎在和什么人用信号灯交流，而那个方向根本不应该有其他船只..."); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("我只是个厨师，但我记得昨晚我准备了丰盛的晚餐。奇怪的是，在开饭前一小时，船长突然宣布所有人必须到甲板上去，说是欣赏什么奇景。这太反常了..."); gameMapper.insertRoleActContent(rc5);
        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act1.getActId()); rc6.setRoleId(r6.getRoleId()); rc6.setContent("作为医生，我注意到船员们的健康状况最近都不太好。我私下调查发现，船上的饮用水似乎被人动过手脚。更诡异的是，我自己配制的急救药品竟然也被人换成了假药..."); gameMapper.insertRoleActContent(rc6);

        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act2.getActId()); rc7.setRoleId(r1.getRoleId()); rc7.setContent("我死里逃生！那把匕首是从背后刺来的，但我清楚地记得昨晚最后看到的人影...是厨师！他那晚拒绝和我们一起用餐，说是在准备什么重要的事情。"); gameMapper.insertRoleActContent(rc7);
        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r2.getRoleId()); rc8.setContent("我找到了一张照片！是船长年轻时的照片，旁边站着一个和厨师长得很像的人。难道船长和厨师之间有什么不为人知的过去？也许这与凶杀案有关！"); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r3.getRoleId()); rc9.setContent("我看到厨师昨晚偷偷潜入了船长的房间，但我以为他只是去偷东西，没想到...等等，船长的保险箱里应该有一份重要文件，但我翻遍了整个房间都没找到。凶手会不会就是冲着那份文件来的？"); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r4.getRoleId()); rc10.setContent("我承认我恨船长，他克扣我们的工资，但我发誓人不是我杀的！我昨晚一直在甲板上值班，有至少三个人可以为我作证！倒是厨师，那晚的行踪非常可疑..."); gameMapper.insertRoleActContent(rc10);
        RoleActContent rc11 = new RoleActContent(); rc11.setScriptId(scriptId); rc11.setActId(act2.getActId()); rc11.setRoleId(r5.getRoleId()); rc11.setContent("什么？怀疑我？我昨晚一直在厨房收拾餐具，根本没离开过！而且你们不知道的是，船长昨晚找过我，问我关于十年前那场海难的事...他对那个话题非常敏感。"); gameMapper.insertRoleActContent(rc11);
        RoleActContent rc12 = new RoleActContent(); rc12.setScriptId(scriptId); rc12.setActId(act2.getActId()); rc12.setRoleId(r6.getRoleId()); rc12.setContent("我检查过船长的尸体，凶器是一把厨房用的剔骨刀。死亡时间大约是昨晚十点到十一点之间。但更让我震惊的是，船长体内有慢性毒药的残留，这说明凶手可能早就开始策划了！"); gameMapper.insertRoleActContent(rc12);

        RoleActContent rc13 = new RoleActContent(); rc13.setScriptId(scriptId); rc13.setActId(act3.getActId()); rc13.setRoleId(r1.getRoleId()); rc13.setContent("现在你们都来了，很好。我就知道，十年前的那件事终究会有人来复仇。不过我没有遗憾了..."); gameMapper.insertRoleActContent(rc13);
        RoleActContent rc14 = new RoleActContent(); rc14.setScriptId(scriptId); rc14.setActId(act3.getActId()); rc14.setRoleId(r2.getRoleId()); rc14.setContent("原来...原来这一切都是为了十年前那场海难！我现在终于明白了，我的未婚夫是怎么死的了..."); gameMapper.insertRoleActContent(rc14);
        RoleActContent rc15 = new RoleActContent(); rc15.setScriptId(scriptId); rc15.setActId(act3.getActId()); rc15.setRoleId(r3.getRoleId()); rc15.setContent("都结束了...十年前的债，终于了。"); gameMapper.insertRoleActContent(rc15);
        RoleActContent rc16 = new RoleActContent(); rc16.setScriptId(scriptId); rc16.setActId(act3.getActId()); rc16.setRoleId(r4.getRoleId()); rc16.setContent("我真不敢相信这是真的，原来我们一直被仇恨蒙蔽了双眼..."); gameMapper.insertRoleActContent(rc16);
        RoleActContent rc17 = new RoleActContent(); rc17.setScriptId(scriptId); rc17.setActId(act3.getActId()); rc17.setRoleId(r5.getRoleId()); rc17.setContent("算了，都过去了...希望以后再也不要有仇恨了。"); gameMapper.insertRoleActContent(rc17);
        RoleActContent rc18 = new RoleActContent(); rc18.setScriptId(scriptId); rc18.setActId(act3.getActId()); rc18.setRoleId(r6.getRoleId()); rc18.setContent("现在最重要的是，我们能一起想办法离开这座岛，活下去！"); gameMapper.insertRoleActContent(rc18);

        ScriptClue clue1 = new ScriptClue(); clue1.setScriptId(scriptId.longValue()); clue1.setClueName("船长的日记"); clue1.setClueDesc("日记最后一页写着：'十年了，他终于来了...'"); clue1.setIsPublic(1); clue1.setUnlockChapterId(act1.getActId().longValue()); gameMapper.insertScriptClue(clue1);
        ScriptClue clue2 = new ScriptClue(); clue2.setScriptId(scriptId.longValue()); clue2.setClueName("带血的围裙"); clue2.setClueDesc("厨师的围裙上有不易察觉的血迹"); clue2.setIsPublic(0); clue2.setUnlockChapterId(act2.getActId().longValue()); clue2.setRoleId(r5.getRoleId()); gameMapper.insertScriptClue(clue2);
        ScriptClue clue3 = new ScriptClue(); clue3.setScriptId(scriptId.longValue()); clue3.setClueName("匿名恐吓信"); clue3.setClueDesc("一封匿名恐吓信，内容是'血债血偿'"); clue3.setIsPublic(0); clue3.setUnlockChapterId(act2.getActId().longValue()); clue3.setRoleId(r1.getRoleId()); gameMapper.insertScriptClue(clue3);
        ScriptClue clue4 = new ScriptClue(); clue4.setScriptId(scriptId.longValue()); clue4.setClueName("十年前的报纸"); clue4.setClueDesc("泛黄的报纸，报道了一场海难"); clue4.setIsPublic(1); clue4.setUnlockChapterId(act3.getActId().longValue()); gameMapper.insertScriptClue(clue4);
        ScriptClue clue5 = new ScriptClue(); clue5.setScriptId(scriptId.longValue()); clue5.setClueName("保险箱钥匙"); clue5.setClueDesc("一把生锈的保险箱钥匙"); clue5.setIsHidden(1); clue5.setUnlockChapterId(act2.getActId().longValue()); clue5.setUnlockCondition("检查海滩礁石附近"); gameMapper.insertScriptClue(clue5);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(r3.getRoleId()); ending1.setEndingTitle("真相大白"); ending1.setEndingContent("你成功指认出了凶手！管家跪倒在地，承认了自己的罪行。原来十年前，船长为了独吞船上的黄金，制造了那场海难，而管家的未婚妻就在那艘船上..."); gameMapper.insertScriptEnding(ending1);
        ScriptEnding ending2 = new ScriptEnding(); ending2.setScriptId(scriptId); ending2.setVotedRoleId(-1); ending2.setEndingTitle("迷雾重重"); ending2.setEndingContent("你们没能找出真正的凶手，岛上的迷雾越来越浓..."); gameMapper.insertScriptEnding(ending2);

        ScriptArchitecture arch1 = new ScriptArchitecture(); arch1.setScriptId(scriptId); arch1.setArchName("废弃灯塔"); arch1.setArchDesc("岛上唯一的建筑，一座废弃多年的灯塔"); gameMapper.insertArchitecture(arch1);
        ScriptArchitecture arch2 = new ScriptArchitecture(); arch2.setScriptId(scriptId); arch2.setArchName("海滩"); arch2.setArchDesc("细软的沙滩，散落着船只残骸"); gameMapper.insertArchitecture(arch2);
    }

    private void createScript2Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("太子"); r1.setBackground("大楚王朝的储君，温文尔雅。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("宰相"); r2.setBackground("权倾朝野的当朝宰相。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("贵妃"); r3.setBackground("皇帝最宠爱的妃子。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("太傅"); r4.setBackground("太子的老师，学问渊博。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("侍卫长"); r5.setBackground("负责宫廷安全的禁军统领。"); gameMapper.insertRole(r5);
        Role r6 = new Role(); r6.setScriptId(scriptId); r6.setName("宫女"); r6.setBackground("贵妃身边的贴身侍女。"); gameMapper.insertRole(r6);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：宫中命案"); act1.setSort(1); act1.setPublicContent("太傅被发现死于书房之中，桌上留有一首绝命诗..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：暗流涌动"); act2.setSort(2); act2.setPublicContent("调查发现，太傅死前曾与多人密谈..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：朝堂对峙"); act3.setSort(3); act3.setPublicContent("真相即将揭晓，所有人齐聚大殿..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("太傅是我的恩师，他的死让我痛心疾首。但更让我不安的是，那首绝命诗似乎暗藏玄机...我隐约记得老师曾说过，'当朝最大的秘密就藏在那幅山水画之后'。"); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("太傅之死确实蹊跷，但眼下最重要的是稳定朝局。那首绝命诗我已经派人仔细研究过了，不过是些牢骚满腹的酸词罢了。不过说起来，太傅死前一天曾来找过我，说有要事相商..."); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("太傅与本宫并无深交，但他的死让我想起了一件事。三天前，太傅曾私下告诉我，他发现了一个惊天的秘密，关于...算了，在这种场合不便多说。"); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("虽然太傅已死，但我这个太子的老师身份还在保护着我。我死前最后见到的人...等等，这不是说死者的事吗？太傅死前那晚，我在御花园看到了宰相，他似乎在销毁什么东西。"); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("作为侍卫长，保护太傅是我的职责所在。太傅死后，我立即封锁了现场。那首绝命诗上的字迹我仔细辨认过，与太傅平日笔迹有细微差异，很可能不是他本人所写！"); gameMapper.insertRoleActContent(rc5);
        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act1.getActId()); rc6.setRoleId(r6.getRoleId()); rc6.setContent("奴婢虽身份卑微，但贵妃娘娘对我恩重如山。那晚太傅来求见娘娘时，我恰好在门外候着，听到了一些...太傅说'通敌的证据就在相府密室'，然后娘娘就让他离开了。"); gameMapper.insertRoleActContent(rc6);

        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act2.getActId()); rc7.setRoleId(r1.getRoleId()); rc7.setContent("我终于明白了！太傅留下的不是绝命诗，而是一张密信！那首诗的每一句第一个字连起来，就是'相府密室'。宰相，你的好日子到头了！"); gameMapper.insertRoleActContent(rc7);
        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r2.getRoleId()); rc8.setContent("太子殿下这是在指控本相吗？笑话！相府密室中确实有些东西，但那都是先帝赐予的密函，与通敌有何关系？倒是太子殿下，似乎对自己的身世很感兴趣啊..."); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r3.getRoleId()); rc9.setContent("本宫知道一些事...太傅临死前来找过我，他说他在宰相府发现了一封密信，内容涉及十年前的一场战役，数十万将士的性命...以及当今圣上的皇位正统性。"); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r4.getRoleId()); rc10.setContent("虽然我是太子，但我最近才知道自己的真实身份...太傅死前告诉我的那个秘密，让我夜不能寐。原来我的亲生父亲另有其人，而那个人被当今圣上囚禁在相府密室之中！"); gameMapper.insertRoleActContent(rc10);
        RoleActContent rc11 = new RoleActContent(); rc11.setScriptId(scriptId); rc11.setActId(act2.getActId()); rc11.setRoleId(r5.getRoleId()); rc11.setContent("我已经调查清楚了。太傅之死绝非自杀，而是被人灭口。凶器是书房中的一把裁纸刀，但更重要的是，我在现场发现了一枚宰相府的腰牌，这说明凶手与宰相府有关！"); gameMapper.insertRoleActContent(rc11);
        RoleActContent rc12 = new RoleActContent(); rc12.setScriptId(scriptId); rc12.setActId(act2.getActId()); rc12.setRoleId(r6.getRoleId()); rc12.setContent("贵妃娘娘让我隐瞒的事，我现在不得不说...那晚贵妃娘娘确实见了太傅，但随后太傅并没有离开，而是在御花园中被人杀害。凶手穿着禁军的服装，但我认出了那个身形...是宰相府的护卫！"); gameMapper.insertRoleActContent(rc12);

        RoleActContent rc13 = new RoleActContent(); rc13.setScriptId(scriptId); rc13.setActId(act3.getActId()); rc13.setRoleId(r1.getRoleId()); rc13.setContent("我是太子，但真相必须大白于天下！"); gameMapper.insertRoleActContent(rc13);
        RoleActContent rc14 = new RoleActContent(); rc14.setScriptId(scriptId); rc14.setActId(act3.getActId()); rc14.setRoleId(r2.getRoleId()); rc14.setContent("成王败寇，无话可说！"); gameMapper.insertRoleActContent(rc14);
        RoleActContent rc15 = new RoleActContent(); rc15.setScriptId(scriptId); rc15.setActId(act3.getActId()); rc15.setRoleId(r3.getRoleId()); rc15.setContent("只愿王朝昌盛！"); gameMapper.insertRoleActContent(rc15);
        RoleActContent rc16 = new RoleActContent(); rc16.setScriptId(scriptId); rc16.setActId(act3.getActId()); rc16.setRoleId(r4.getRoleId()); rc16.setContent("真相终于可以告慰太傅在天之灵了！"); gameMapper.insertRoleActContent(rc16);
        RoleActContent rc17 = new RoleActContent(); rc17.setScriptId(scriptId); rc17.setActId(act3.getActId()); rc17.setRoleId(r5.getRoleId()); rc17.setContent("我会守护好皇宫的安全！"); gameMapper.insertRoleActContent(rc17);
        RoleActContent rc18 = new RoleActContent(); rc18.setScriptId(scriptId); rc18.setActId(act3.getActId()); rc18.setRoleId(r6.getRoleId()); rc18.setContent("愿娘娘安好！"); gameMapper.insertRoleActContent(rc18);

        ScriptClue clue1 = new ScriptClue(); clue1.setScriptId(scriptId.longValue()); clue1.setClueName("绝命诗"); clue1.setClueDesc("一首奇怪的诗，每句首字连起来是'相府密室'"); clue1.setIsPublic(1); clue1.setUnlockChapterId(act1.getActId().longValue()); gameMapper.insertScriptClue(clue1);
        ScriptClue clue2 = new ScriptClue(); clue2.setScriptId(scriptId.longValue()); clue2.setClueName("宰相腰牌"); clue2.setClueDesc("一枚刻有宰相府字样的腰牌"); clue2.setIsPublic(0); clue2.setRoleId(r5.getRoleId()); clue2.setUnlockChapterId(act1.getActId().longValue()); gameMapper.insertScriptClue(clue2);
        ScriptClue clue3 = new ScriptClue(); clue3.setScriptId(scriptId.longValue()); clue3.setClueName("密信残片"); clue3.setClueDesc("被烧毁的密信，隐约可见'通敌'二字"); clue3.setIsPublic(1); clue3.setUnlockChapterId(act2.getActId().longValue()); gameMapper.insertScriptClue(clue3);
        ScriptClue clue4 = new ScriptClue(); clue4.setScriptId(scriptId.longValue()); clue4.setClueName("山水画轴"); clue4.setClueDesc("书房墙上的山水画，画轴有机关"); clue4.setIsHidden(1); clue4.setUnlockChapterId(act2.getActId().longValue()); clue4.setUnlockCondition("仔细检查书房墙壁"); gameMapper.insertScriptClue(clue4);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(r2.getRoleId()); ending1.setEndingTitle("奸臣伏法"); ending1.setEndingContent("宰相的阴谋被揭穿，皇帝龙颜大怒，将其打入天牢..."); gameMapper.insertScriptEnding(ending1);
        ScriptEnding ending2 = new ScriptEnding(); ending2.setScriptId(scriptId); ending2.setVotedRoleId(-1); ending2.setEndingTitle("朝堂秘辛"); ending2.setEndingContent("有些秘密永远不会被揭开..."); gameMapper.insertScriptEnding(ending2);

        ScriptArchitecture arch1 = new ScriptArchitecture(); arch1.setScriptId(scriptId); arch1.setArchName("御书房"); arch1.setArchDesc("太傅办公的地方，命案现场"); gameMapper.insertArchitecture(arch1);
        ScriptArchitecture arch2 = new ScriptArchitecture(); arch2.setScriptId(scriptId); arch2.setArchName("御花园"); arch2.setArchDesc("宫中花园，曾有人在此密会"); gameMapper.insertArchitecture(arch2);
        ScriptArchitecture arch3 = new ScriptArchitecture(); arch3.setScriptId(scriptId); arch3.setArchName("金銮殿"); arch3.setArchDesc("朝堂对峙的地方"); gameMapper.insertArchitecture(arch3);
    }

    private void createScript3Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("大儿子"); r1.setBackground("做生意发了财，想独占家产。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("大儿媳"); r2.setBackground("精明能干，算盘打得精。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("二女儿"); r3.setBackground("远嫁外地，突然回来争家产。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("小儿子"); r4.setBackground("啃老族，游手好闲。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("小女儿"); r5.setBackground("最孝顺的孩子，默默照顾父亲。"); gameMapper.insertRole(r5);
        Role r6 = new Role(); r6.setScriptId(scriptId); r6.setName("女婿"); r6.setBackground("二女儿的丈夫，鬼点子多。"); gameMapper.insertRole(r6);
        Role r7 = new Role(); r7.setScriptId(scriptId); r7.setName("保姆"); r7.setBackground("照顾老王头多年的保姆。"); gameMapper.insertRole(r7);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：拆迁通知"); act1.setSort(1); act1.setPublicContent("拆迁通知下来了，老王家炸开了锅..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：老王失踪"); act2.setSort(2); act2.setPublicContent("老王头突然不见了，大家开始互相猜忌..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：真相大白"); act3.setSort(3); act3.setPublicContent("小女儿在阁楼找到了老王头..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("我是大儿子，这笔拆迁款我势在必得！我做生意亏了钱，急需这笔钱周转。二妹、三弟他们都嫁出去了，凭什么回来分钱？倒是小妹，每天守着老头子，谁知道她有没有私吞什么..."); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("你们别看我婆婆人好说话，私底下精明着呢！我嫁进来这些年，明里暗里补贴了家里多少钱。这拆迁款，我大儿子那份，一分都不能少！倒是那老头子最近可疑，总是一个人躲在房间里数钱..."); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("远嫁这么多年，我受够了！现在拆迁了，老头子却不肯见我，肯定是大嫂在背后嚼舌根！说实话，这次回来我就是冲着钱来的。老公，你那个主意靠谱吗？别到时候钱没拿到，人先进去了..."); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("拆迁款？那玩意儿我不在乎，反正老爷子最疼我，就算不分我也会给我留一份。现在最让我不爽的是，保姆阿姨最近对我爱答不理的，她该不会是想独吞老爷子的私房钱吧？"); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("你们都只想着钱，可曾关心过老爷子的身体？他最近身体越来越差了...我不想争什么，只希望他能安享晚年。倒是最近我发现二姐和姐夫鬼鬼祟祟的，不知道在商量什么..."); gameMapper.insertRoleActContent(rc5);
        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act1.getActId()); rc6.setRoleId(r6.getRoleId()); rc6.setContent("二妹的丈夫，就是我。这次回来可不是吃素的！我有个朋友在拆迁办，有点关系。只要运作得当，咱能比其他人多拿三成。不过这事得保密，大哥他们肯定不同意..."); gameMapper.insertRoleActContent(rc6);
        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act1.getActId()); rc7.setRoleId(r7.getRoleId()); rc7.setContent("我在老王家这么多年，老王头的脾气我最清楚。他突然不见踪影，肯定和这拆迁款有关。他昨晚偷偷告诉我，他有一张存折，藏在一个谁也想不到的地方。还说...算了，老王头不让我告诉别人。"); gameMapper.insertRoleActContent(rc7);

        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r1.getRoleId()); rc8.setContent("老头子失踪了？这肯定是大哥搞的鬼！他一直想独占拆迁款，说不定是把老头子藏起来了！大嫂，你最好让你老公把老爷子交出来，否则别怪我报警！"); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r2.getRoleId()); rc9.setContent("二妹你别血口喷人！我老公昨晚一直在家，哪也没去！倒是你和你那个鬼丈夫，行踪诡异得很。说起来，老头子失踪前，最怀疑的人就是你们两个..."); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r3.getRoleId()); rc10.setContent("大嫂你别转移视线！我昨晚确实出门了，但那是去我朋友家借宿。倒是小妹你，一直和老爷子住在一起，他失踪了你反而最淡定？这事太蹊跷了！"); gameMapper.insertRoleActContent(rc10);
        RoleActContent rc11 = new RoleActContent(); rc11.setScriptId(scriptId); rc11.setActId(act2.getActId()); rc11.setRoleId(r4.getRoleId()); rc11.setContent("你们别吵了行不行？我昨晚喝多了，什么都不知道！不过说起来，老头子失踪前两天，突然把他所有的首饰都给了小妹，这里面肯定有猫腻..."); gameMapper.insertRoleActContent(rc11);
        RoleActContent rc12 = new RoleActContent(); rc12.setScriptId(scriptId); rc12.setActId(act2.getActId()); rc12.setRoleId(r5.getRoleId()); rc12.setContent("老爷子确实失踪了，但...我想我知道他在哪里。他走之前让我不要声张，说是要考验所有人。我答应了他，现在还不能说...但是我知道，他很安全，而且他一直在看着我们。"); gameMapper.insertRoleActContent(rc12);
        RoleActContent rc13 = new RoleActContent(); rc13.setScriptId(scriptId); rc13.setActId(act2.getActId()); rc13.setRoleId(r6.getRoleId()); rc13.setContent("小妹你肯定知道什么！别装了！我看这就是一出闹剧，老头子根本没失踪，是你们自导自演的吧？老公，把那个'计划B'准备好..."); gameMapper.insertRoleActContent(rc13);
        RoleActContent rc14 = new RoleActContent(); rc14.setScriptId(scriptId); rc14.setActId(act2.getActId()); rc14.setRoleId(r7.getRoleId()); rc14.setContent("我照顾老王头这么多年，我最了解他。他不是失踪，是'躲'起来了。昨晚我看到他半夜在客厅自言自语，说'我倒要看看这群孩子有没有良心'。他这是在考验你们呢！"); gameMapper.insertRoleActContent(rc14);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(0); ending1.setEndingTitle("家和万事兴"); ending1.setEndingContent("一家人终于明白亲情的重要，决定平分拆迁款，共同照顾老王头..."); gameMapper.insertScriptEnding(ending1);
    }

    private void createScript4Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("队长"); r1.setBackground("探险队队长，胆大心细。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("摄影师"); r2.setBackground("负责拍摄的摄影师。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("主播"); r3.setBackground("团队的颜值担当。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("向导"); r4.setBackground("本地村民，熟悉山路。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("村长"); r5.setBackground("神秘的山村村长。"); gameMapper.insertRole(r5);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：诡异山村"); act1.setSort(1); act1.setPublicContent("探险队来到偏僻山村，村民们眼神怪异..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：夜半哭声"); act2.setSort(2); act2.setPublicContent("半夜，破庙方向传来女人的哭声..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：揭开真相"); act3.setSort(3); act3.setPublicContent("真相浮出水面，女鬼的真实身份是..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("这个村子太诡异了！一进村我就感觉到不对劲。村民们看我们的眼神...像是在看猎物。特别是村长，他的笑容让我后背发凉。还有那个向导，一路上都在念叨什么，像是在祈祷，又像是在诅咒。"); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("我已经拍了不少素材，但总觉得哪里不对劲。今晚我无意中拍到了一张照片，在村长家的大门后面，藏着一张符咒...和我们在破庙里看到的一模一样！这个村子肯定有什么秘密！"); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("哎呀，这个村子好阴森啊！我直播间的观众都说这里有鬼气。不过说实话，我总觉得向导有点问题，他好像认识我们中的某个人...还偷偷问我，主播小姐，你是不是本村出去的人？我当然不是啦！但他为什么要这么问？"); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("你们这些外人不知道，这座山上的破庙邪门得很。二十年前，那里死过一个女人。从那以后，每到月圆之夜，就能听到哭声。我劝你们最好别去招惹那些东西...村长是我的远房表亲，他让我盯着你们。"); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("欢迎来到我们村！我当村长已经三十年了。那个破庙是村里的禁地，你们最好别去。至于那个女鬼的故事，不过是老一辈吓唬小孩的玩意儿。不过说起来，最近确实有些奇怪的事情发生...我在调查村里的失踪案。"); gameMapper.insertRoleActContent(rc5);

        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act2.getActId()); rc6.setRoleId(r1.getRoleId()); rc6.setContent("那哭声是真的！我半夜被吵醒了，声音是从破庙方向传来的。我壮着胆子过去查看，结果发现...庙门开着，里面空无一人，但地上有新鲜的血迹！这到底是怎么回事？！"); gameMapper.insertRoleActContent(rc6);
        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act2.getActId()); rc7.setRoleId(r2.getRoleId()); rc7.setContent("我查看了我拍的照片，发现了一个可怕的事实！在队长去破庙之前，照片里根本没有血迹。但当我放大查看时...背景里有一个白色的影子！这个女人是谁？她为什么会出现在照片里？"); gameMapper.insertRoleActContent(rc7);
        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r3.getRoleId()); rc8.setContent("太恐怖了！我直播间的观众都吓坏了。有人说那个女鬼其实是人假扮的，想吓跑我们这些外地人。等等，我想起来了，向导说过，'后山那个女人'...难道主播长得像那个女人？难怪向导一直盯着我看！"); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r4.getRoleId()); rc9.setContent("完了，完了！女鬼出来了！那个女人...她在找她的孩子！二十年前，她的孩子被村里人...不是，她不是鬼，她是...村长不让我说！因为他就是当年那件事的参与者！"); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r5.getRoleId()); rc10.setContent("你们不应该来的。那个女人的怨灵一直困在这座山里，她要复仇。二十年前的那场悲剧...是我父亲那一辈人犯下的错。他们为了掩盖一个秘密，害死了她和孩子。现在她回来了，带着无尽的怨恨。"); gameMapper.insertRoleActContent(rc10);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(5); ending1.setEndingTitle("冤屈昭雪"); ending1.setEndingContent("村长的女儿跪在母亲坟前，终于说出了真相..."); gameMapper.insertScriptEnding(ending1);
    }

    private void createScript5Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("技术总监"); r1.setBackground("公司技术核心，压力巨大。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("产品经理"); r2.setBackground("需求变更多，被开发吐槽。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("后端开发"); r3.setBackground("资深程序员，代码功底深厚。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("前端开发"); r4.setBackground("经常熬夜改bug。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("实习生"); r5.setBackground("刚入职的大学生。"); gameMapper.insertRole(r5);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：上线前夜"); act1.setSort(1); act1.setPublicContent("距离产品上线只剩几小时，大家都在加班..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：服务器炸了"); act2.setSort(2); act2.setPublicContent("服务器突然崩溃，所有数据消失..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：代码追责"); act3.setSort(3); act3.setPublicContent("必须找出是谁删库跑路..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("压力太大了！上线失败的话，整个产品线都得被砍。今晚我必须确保所有服务正常运行。等等，我好像看到实习生在服务器旁边鬼鬼祟祟的...希望他没乱动什么。"); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("需求是我提的，但代码是你们写的！为什么总是延期？我也是为公司好嘛...说起来，我注意到产品经理办公室的电脑最近总是半夜亮着，不知道是谁在使用。"); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("代码我已经 review 过了，没问题。但...我注意到有人半夜改过数据库配置。我查了日志，是一个叫 'product_manager' 的账号。不是吧，产品经理有服务器权限？"); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("前端页面早就调好了，是后端接口太慢！今晚加班太累了，我打了个盹。醒来时发现实习生在运维服务器旁边转悠...算了，我什么都没看见。"); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("我只是个实习生，今晚负责给大家买咖啡和订外卖！虽然技术总监让我'不要碰服务器'，但我只是去拿咖啡的时候路过看了一眼...服务器状态灯是绿色的，应该没问题吧？"); gameMapper.insertRoleActContent(rc5);

        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act2.getActId()); rc6.setRoleId(r1.getRoleId()); rc6.setContent(" rm -rf /* ？这是哪个天才执行的命令？！等等，我查了日志...这个命令是产品经理的账号执行的！但产品经理怎么会知道 root 密码？除非...有人盗用了他的账号！"); gameMapper.insertRoleActContent(rc6);
        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act2.getActId()); rc7.setRoleId(r2.getRoleId()); rc7.setContent("什么？删库？我什么都没做！虽然我和技术总监有矛盾，但我至于这么极端吗？而且...我确实知道服务器 root 密码，因为上周运维教过我如何紧急恢复数据。但我真的没有用过！"); gameMapper.insertRoleActContent(rc7);
        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r3.getRoleId()); rc8.setContent("我在查日志时发现了一件恐怖的事：那个删库命令的来源 IP 是...产品经理家的地址！但产品经理明明说自己在公司加班。等等，那天他在会议室和投资人开会，投屏了他的电脑...天哪！"); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r4.getRoleId()); rc9.setContent("我找到了一条关键线索！在崩溃前十分钟，服务器上有人安装了一个计划任务，定时执行一个脚本。那个脚本的内容是：'rm -rf /var/lib/mysql/*'。这不是意外，这是蓄意谋杀！"); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r5.getRoleId()); rc10.setContent("我不知道什么删库不删库的，但...我记得我拿咖啡回来的时候，看到产品经理在前端开发的工位上，而前端开发当时去厕所了！产品经理在对着电脑敲什么命令行，表情很阴沉...但我不敢说什么，毕竟他是经理。"); gameMapper.insertRoleActContent(rc10);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(2); ending1.setEndingTitle("职场阴谋"); ending1.setEndingContent("产品经理的阴谋被揭穿，他被公司开除..."); gameMapper.insertScriptEnding(ending1);
    }

    private void createScript6Components(Integer scriptId) {
        Role r1 = new Role(); r1.setScriptId(scriptId); r1.setName("伯爵夫人"); r1.setBackground("年轻貌美的伯爵夫人。"); gameMapper.insertRole(r1);
        Role r2 = new Role(); r2.setScriptId(scriptId); r2.setName("医生"); r2.setBackground("伯爵的私人医生。"); gameMapper.insertRole(r2);
        Role r3 = new Role(); r3.setScriptId(scriptId); r3.setName("律师"); r3.setBackground("伯爵的法律顾问。"); gameMapper.insertRole(r3);
        Role r4 = new Role(); r4.setScriptId(scriptId); r4.setName("女仆"); r4.setBackground("在古堡工作多年。"); gameMapper.insertRole(r4);
        Role r5 = new Role(); r5.setScriptId(scriptId); r5.setName("侄子"); r5.setBackground("伯爵的侄子，觊觎遗产。"); gameMapper.insertRole(r5);
        Role r6 = new Role(); r6.setScriptId(scriptId); r6.setName("管家"); r6.setBackground("古堡的老管家。"); gameMapper.insertRole(r6);
        Role r7 = new Role(); r7.setScriptId(scriptId); r7.setName("神父"); r7.setBackground("附近教堂的神父。"); gameMapper.insertRole(r7);

        Act act1 = new Act(); act1.setScriptId(scriptId); act1.setActName("第一幕：古堡晚宴"); act1.setSort(1); act1.setPublicContent("伯爵邀请各位来到古堡参加晚宴..."); gameMapper.insertAct(act1);
        Act act2 = new Act(); act2.setScriptId(scriptId); act2.setActName("第二幕：午夜钟声"); act2.setSort(2); act2.setPublicContent("十二点的钟声敲响，伯爵被发现死于密室..."); gameMapper.insertAct(act2);
        Act act3 = new Act(); act3.setScriptId(scriptId); act3.setActName("第三幕：破解谜案"); act3.setSort(3); act3.setPublicContent("侦探必须在天亮前找出凶手..."); gameMapper.insertAct(act3);

        RoleActContent rc1 = new RoleActContent(); rc1.setScriptId(scriptId); rc1.setActId(act1.getActId()); rc1.setRoleId(r1.getRoleId()); rc1.setContent("伯爵总是疑神疑鬼的，说什么要立遗嘱。今天晚宴上，他看我的眼神特别复杂...好像在怀疑我什么。但我真的不知道他为什么突然邀请这么多人来古堡。侄子突然回来...难道是遗产之争？"); gameMapper.insertRoleActContent(rc1);
        RoleActContent rc2 = new RoleActContent(); rc2.setScriptId(scriptId); rc2.setActId(act1.getActId()); rc2.setRoleId(r2.getRoleId()); rc2.setContent("伯爵最近身体很不好，我给他开了不少药。但奇怪的是，他昨晚突然拒绝服药，说有人在药里下毒！他还让我把药方给他看...说实话，我也觉得这个古堡里气氛诡异得很。"); gameMapper.insertRoleActContent(rc2);
        RoleActContent rc3 = new RoleActContent(); rc3.setScriptId(scriptId); rc3.setActId(act1.getActId()); rc3.setRoleId(r3.getRoleId()); rc3.setContent("伯爵立遗嘱的事是保密的，但遗嘱内容我已经起草好了。大部分财产给夫人，但...侄子也有一笔可观的遗产。我注意到侄子最近和伯爵夫人走得很近，不知道在密谋什么。"); gameMapper.insertRoleActContent(rc3);
        RoleActContent rc4 = new RoleActContent(); rc4.setScriptId(scriptId); rc4.setActId(act1.getActId()); rc4.setRoleId(r4.getRoleId()); rc4.setContent("我在古堡工作了二十年，从没见过伯爵这么紧张。今晚他说要做个实验，让所有人待在自己房间，等到十二点再出来。可我分明看到...医生半夜偷偷溜进了伯爵的书房！"); gameMapper.insertRoleActContent(rc4);
        RoleActContent rc5 = new RoleActContent(); rc5.setScriptId(scriptId); rc5.setActId(act1.getActId()); rc5.setRoleId(r5.getRoleId()); rc5.setContent("叔叔突然叫我回来，肯定是有大事发生。但他的态度让我很不满...明显偏心那个女人！今晚我要和他好好谈谈，如果谈不拢...哼，这座古堡早晚是我的！"); gameMapper.insertRoleActContent(rc5);
        RoleActContent rc6 = new RoleActContent(); rc6.setScriptId(scriptId); rc6.setActId(act1.getActId()); rc6.setRoleId(r6.getRoleId()); rc6.setContent("伯爵这几天脾气暴躁得很，说有人在监视他。作为管家，我必须保护他的安全。今晚的钟楼，我亲自检查过了，一切正常。但我注意到...钟楼的门锁被动过！"); gameMapper.insertRoleActContent(rc6);
        RoleActContent rc7 = new RoleActContent(); rc7.setScriptId(scriptId); rc7.setActId(act1.getActId()); rc7.setRoleId(r7.getRoleId()); rc7.setContent("伯爵邀请我来参加晚宴，说是'有重要的事'。我只是一个神父，不太参与俗世的事务...但今晚伯爵私下告诉我，他发现了这座古堡的一个秘密，关于这座钟楼的...然后他就死了。"); gameMapper.insertRoleActContent(rc7);

        RoleActContent rc8 = new RoleActContent(); rc8.setScriptId(scriptId); rc8.setActId(act2.getActId()); rc8.setRoleId(r1.getRoleId()); rc8.setContent("伯爵死了！就在十二点整！当我赶到现场时，书房的门从里面锁着，窗户也反锁...这是一间完美的密室！而医生...他就在门外，说在等伯爵召见...他嫌疑最大！"); gameMapper.insertRoleActContent(rc8);
        RoleActContent rc9 = new RoleActContent(); rc9.setScriptId(scriptId); rc9.setActId(act2.getActId()); rc9.setRoleId(r2.getRoleId()); rc9.setContent("我检查过尸体了！伯爵是死于毒药，不是被匕首刺死的。但奇怪的是，他的胸口确实有一把匕首...等等，这不是凶器，这是...误导！凶器是那杯酒！我亲眼看到侄子给伯爵倒的酒！"); gameMapper.insertRoleActContent(rc9);
        RoleActContent rc10 = new RoleActContent(); rc10.setScriptId(scriptId); rc10.setActId(act2.getActId()); rc10.setRoleId(r3.getRoleId()); rc10.setContent("密室杀人...这不科学！除非...有人从密道进入。我突然想起一件事：这座古堡的地下室有一条古老的密道，是当年用来躲避追杀的。除了管家，没人知道这条路！"); gameMapper.insertRoleActContent(rc10);
        RoleActContent rc11 = new RoleActContent(); rc11.setScriptId(scriptId); rc11.setActId(act2.getActId()); rc11.setRoleId(r4.getRoleId()); rc11.setContent("伯爵死的时候，我就在门外不远处！我看到...管家半夜从地下室的入口出来，手里还拿着什么东西！而那个入口...只有管家有钥匙！"); gameMapper.insertRoleActContent(rc11);
        RoleActContent rc12 = new RoleActContent(); rc12.setScriptId(scriptId); rc12.setActId(act2.getActId()); rc12.setRoleId(r5.getRoleId()); rc12.setContent("你们怀疑我？可笑！我确实给叔叔倒了酒，但那酒他没喝！因为...女仆突然说有人来访，叔叔就放下酒杯出去了。等他回来后，我已经离开房间了！那酒...难道有问题？"); gameMapper.insertRoleActContent(rc12);
        RoleActContent rc13 = new RoleActContent(); rc13.setScriptId(scriptId); rc13.setActId(act2.getActId()); rc13.setRoleId(r6.getRoleId()); rc13.setContent("你们都错了！密道确实存在，但今晚没人从那里进去过。因为...我亲自把密道入口封死了！那入口在哪？就在...不，我不能说出来，那会暴露更大的秘密！"); gameMapper.insertRoleActContent(rc13);
        RoleActContent rc14 = new RoleActContent(); rc14.setScriptId(scriptId); rc14.setActId(act2.getActId()); rc14.setRoleId(r7.getRoleId()); rc14.setContent("伯爵临死前告诉我，他发现了这座古堡的钟楼里藏着一个惊人的秘密...他本来打算今晚告诉大家，但他被杀了！凶手就在我们之中！而且，凶手利用了钟楼的机关...那个机关，我知道怎么用。"); gameMapper.insertRoleActContent(rc14);

        ScriptEnding ending1 = new ScriptEnding(); ending1.setScriptId(scriptId); ending1.setVotedRoleId(2); ending1.setEndingTitle("完美犯罪"); ending1.setEndingContent("医生的精密计划被揭穿，原来他利用了时钟机关..."); gameMapper.insertScriptEnding(ending1);
    }
}