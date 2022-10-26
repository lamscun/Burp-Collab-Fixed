package burp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gui.ava.html.image.generator.HtmlImageGenerator;

import payint.PayIntConnector;
public class MainClass {
	
	public static void main(String[] args) throws URISyntaxException, IOException {
		
//		String[] list_random_biids = {"g8zhf3fhiA04IZyUksUl8IM5yQDtUGZMFhWQ0Zwba7k%3d -- l2oayratq3wrna9hjnqvoge3quwkk9.oastify.com","dapbxfDesRu8uCRM3YKjiujbXEtT5H5QWJRYRHWfk3g%3d -- qicjsvj17uupxp822mz46wuyppvfj4.oastify.com","Lv5%2bUD2sn9%2bcgWr7OmI2catzqq6Aj2WvbS7Zk0NIqzQ%3d -- woo4v23fmnc93q5fjab3m7mz0q6gu5.oastify.com","0rqBpwTsnjmZY6OnR3Wk3aJnK8KAF2m3lECbaUZ6%2f38%3d -- 9s2a0fj03zhar54y1o2nhat3cuik69.oastify.com","tvPrEqG3356D6LPInQ54DlRzB9Kp50rsSp2BvY66y8c%3d -- zux7p7f517wh7y7tpfzr3snnkeq4et.oastify.com","SG93d%2by%2fj1pcIpLlII2OpRm8VvZ9tsRCbL5peqmTHy4%3d -- 2dg3l5lulggzky8c73jn3q3cs3ytmi.oastify.com","zIP%2fFwXoEhRlQZjLP1IyCXMetbdBnzbLd0I4c8JiKNg%3d -- i4nbf4ezyem3dgwxxl9v6tdtikoacz.oastify.com","o06hqWFy52lk6u%2fYWRqOl0a4ZlCKJQgelyDq3E97FX8%3d -- uki2ba9lx5gc46utvescuijublhb50.oastify.com","92mgjZMHNFne62DWesaFxFx9E2UhZUgtGzvVUljPAX0%3d -- eq8o0w632vld6kcqv4jfk8v7zy5otd.oastify.com","gf%2bi3UoCU%2bqzjWo3iQ%2fLhrkd8KYvCX%2fTA%2fob0HpEqRk%3d -- x1z8ijtd6f5zf7s7betx8w8i79dz1o.oastify.com","pnRZkS1hYcC5vHRj2l3Ig6JqtoUIRfqzWJCMI5f87wA%3d -- rc7ytytm59lmnqu3lbeui61s9jf93y.oastify.com","iR0E%2bhPpYsOhv2iEB%2fSuE3JYFTD9SDCAKR1Z8VyaVHQ%3d -- rc7bhuodk21vkzl7887jdw1zdqjg75.oastify.com","14vvhRJS7MW7cZswVcLzyXtSorpSCEClGNF8fWE9%2bdw%3d -- ar5ikpc3nb0quy2wi3vnbjdtjkpadz.oastify.com","NvO7jCZSfkIeTSY%2bJJ9677OkknC%2fj5VcUKVxx4F%2f6Oc%3d -- 48aczit8p4dtg9fytp9yqwsublhb50.oastify.com","HLbL3ig8BeD7Sh1ZKKJ3zFfReKGU9xwQoxvdrBFwHoc%3d -- msirnonq5rkvbciflrncipinsey4mt.oastify.com","1tCKa8dfFAO4AhGbBxLCBmpI%2bBvFF2eYwXc7QPEGYc8%3d -- n8wagnz2rai8w66a1bmihgauyl4bs0.oastify.com","pP1OB%2bEoQW6hYhqV0pt1QIuuItz3J1lIMl5zsvxbO10%3d -- hsdksmffwvjcq0432s91t453fulk99.oastify.com","nVszODzYN6WoCs9yjLriALPnJUBaVqV8kihygU%2b7ZZA%3d -- ao2qepex3fg9pyqhdvu7ngikpbv1jq.oastify.com","bW9bXgGebEIbKeoQr78SIGHljOUVJ7T%2fSC0mlx60l74%3d -- q1srxttrjw7rishrrn3gfg2iy94zso.oastify.com","iozsuABZP97DHQ7Vm9uix3Q6gGmXzxLKmgK3qIlm9rg%3d -- y98110ga1batryi95n49wvtk3b91xq.oastify.com","WXow6u9y%2btKOZJDm5OrWWleWyegR3Bgonx2nFc0rUX4%3d -- ua81l2rcgqpcas74c4k9emhrfil89x.oastify.com","oDYufqvnZemdyEY%2bcH29uUteg7ydyBPh9I7fK7%2b7q88%3d -- zdgn1wz9473buqaip1rjpxo6nxtnhc.oastify.com","wdjte9kOwnYhZapl5Ucm2QVU%2buGdzQw%2fPloWi8zCnO8%3d -- ymob5kh3p43v89ud4vrmgddywp2fq4.oastify.com","5BA0OZ18M7hv3c8q3DUhFwqrQUwtR2DRODs1o4dndhM%3d -- 7dlmdaovrg9v781mha3yam0u7ldb10.oastify.com","lTKqBFOSEdIFEzRbnz0AkClv%2fSS1Fi8UnXoA9iAcDLY%3d -- kocanh6gy0abxxfswxd9r0c8uz0poe.oastify.com","z4Rer0Tvh55k4WA50Uflbp%2fR%2fobjAiTCmQjbLTMNisI%3d -- cyeta5y0qabjzj82bm14r5ikrbx1lq.oastify.com","u0dm3zYNpn1cD50%2fL95goCqAEDaJ%2bAelxMX7nOJ5Ec8%3d -- 6dkqeyklsxe8lgdw6me7rkpb72ds1h.oastify.com","pFL1UNU5j8mRgkxFz%2bOK2QcTz59GJGdkmsTHL7DyHXc%3d -- 8irprgm90zs2herfem1py1vqdhj77w.oastify.com","VVyBpzhivexigzllsF%2fy8NYrsVNA2v%2fnXZ9X%2fqGY9P0%3d -- the7qfueaw6ww3b929075kfl5cb2zr.oastify.com","qM2zcqeMFEZXSSD0%2fEH%2bJhzLfsCIZwA4xip7vpgZWe8%3d -- v73e4d90f3fuq1wj8rgi9fky0p6fu4.oastify.com","ErWLa2U%2fh4Tdx0vZ4nbDXZWDOox6nn2BwRCclOiPY7U%3d -- lf4t1avkh44fyk2dzfmhquslcci26r.oastify.com","gcGNPh3XTj%2bZzmdAF%2bfm1pwS3jUC%2blKvUFa0bae1TIs%3d -- zjm18otv4w57vmr8b23ulqu5vw1mpb.oastify.com","FOWy5hrz%2fuQuwEor2bKRcMudcWo39U9qF1biEdAJvwQ%3d -- 6v28xhg3zh3402th5rbomil1rsxil7.oastify.com","xiB27M5f3s51l0Nfvevwo4BcPYKLLDaJgJ16TFHwQZQ%3d -- pngevy0i59baloyfc7yqycpvpmvcj1.oastify.com","pZKaqhqqHnvET3IK90B5Xd7qLD7tlav9BPkJsT5OIx8%3d -- bhwubpdqskqqcmv3zfo5p66cl3rtfi.oastify.com","VuY6cGRUMlabjMAAD7AvxSvLWIMLwpcFZW0%2f7O6ICiA%3d -- 9lv1dq1wxkhswbejlp8uaen86zcp0e.oastify.com","KpYxHWZ5VqGWC8tEwC2x%2b3J2y4G1sntcXWay%2f4Hr7VM%3d -- 2qtvkynn2w07kv0lheedaiuaz15rtg.oastify.com","fJHYxaumvak7Jids51P6Ttoql5bh81nJgSzuSWmKvA8%3d -- d4ix94njamp0jxeh0e8wmwa950bqzf.oastify.com","IhKjVO4N5dLlLw8vea3v4ZEpnfTuFJMdMpau0UyQux4%3d -- 6krlfctc9jbmdndwltemnxjpagg64v.oastify.com","XYeFAyfCMoUP5M5jx4iojZD%2bG84JSj7eiOeeAJj3ZtA%3d -- nh87mt01tnhz5uzcyqlrbwnmjdp3ds.oastify.com","T9bjKv%2bskL8fCtmhkATam0uvRJ6aC4W1aNxRZqidi1M%3d -- sb77tc9jzuhyxlvhl4arf8yhk8qyen.oastify.com","5pJNSWni3NqX6fKHcQYmIhdZQ8464kdY7sPEdt35jYg%3d -- c2f1zk9o2v5m3jpx5kk9kuol6cc20r.oastify.com","8vGGFn3j79HT4Q2lVxk3AeoQ9na1cqYb8T%2fMy18ZUVY%3d -- 288o1ucq8ktt1zqfydcd7i0xmoseg3.oastify.com","dbLaJroPqLKrlyua4tEAA3KP9FGrCpnvI%2bX35K5%2bdZc%3d -- rlg3f0vsmghl1e2iefdwkyf04rahy6.oastify.com","9SXevm7yc7PbqfXhdPIciXk6V19OzYje%2fRNbyFL63zU%3d -- o9y0i6h59lhcv8mgg6c3hza9309qxf.oastify.com","UYUwEi0UJAlDL9C7xYwKQ9UrrvAotWNs%2fOkGqYnVhtw%3d -- qgat9o2s6lh6umfyh0vyai70qrwhk6.oastify.com","NsPhCqbY2%2bBr9hf35SkSv8iY%2bEJ%2bN4WNFoAsjR30sOQ%3d -- 8irfifs86ipm1eh89f1y2ne9b0hq5f.oastify.com","JDfNs%2fwaU0CgYsrkMaP2Sx21PVmMN%2b38Yd5ApU7J6XE%3d -- egy3mxbqyohwdx780nb8caf4dvjl7a.oastify.com","qFEW6gsnYC3Io2ntp06g%2f1XdLTpfq1yLErOtnlnP72U%3d -- gwg0l6lshiuy4gbxlsgd7g9fz65wtl.oastify.com","OW49nkUz1%2foIZt4NZv5c6sqbIn4BcVyiKvbyphgf5%2bI%3d -- sfsfdsf.moen4jgf0clyiqyp4qsf2ulsijo9cy.oastify.com","fX7cuPaz7V6W%2bFsp1OU9litdCVcm3gTBAde9g21KGA4%3d -- fk35nsogl6gup0plqkdv9biwzn5dt2.oastify.com","RRpM1FmGbKiby5UOBwjWLX5kzcCQ5QuL96hgm8X7Oyc%3d -- 3lp9c8frv701knfz08czqi925tbjz8.oastify.com","Lj%2fR20dD2IBifoo7%2bndgEPqEMGbCf%2fUUIPph0365N14%3d -- hf0nh6d07sr88e7f9yl7vkmdo4uuij.oastify.com","OyhMQZrRDjMfUL9kNt8jiq5SIbod9R7%2bs0i9M11B7bY%3d -- uca3yd7z061mc9setewvgshem5svgk.oastify.com","2v6t2%2bE2RKxepAB8pQEtgJyM1DD9dG%2fhxQfniowzxq0%3d -- 9u4h52fdjqx3omuj3jtl03fv4macy1.oastify.com","p1s%2bG7oNkQ4Ssrd%2b%2fYEoKjQ%2bdVXP2V8eUs3MYYdxKio%3d -- xhim80uhs1jcrqut47bze98f66cw0l.oastify.com","CA1Vba3UC1FjYuD9AN6PlCoWuleVtYMcKLz7485BEDI%3d -- 7ai0pfniyieybfrqqumpmxzlico2cr.oastify.com","e2vZxDm770pS4zv8yBDmsH%2fAjV8aM1K8snk284EMGSQ%3d -- o5usx37ki72z52ekr1002jhsejk98y.oastify.com","xFwosAa2YBvFOeC8SSMu%2b%2f09TnDWvILgueHczpTVb8Q%3d -- 3z3t59dy1vc927kmdxc00wzmud03os.oastify.com","uP2aS6nJPDkwAw%2fYmNLR7zauVsqFEQl5w5drxEqBEm0%3d -- pha65s2srll2i2sjh3b56zt3uu0ko9.oastify.com","YiLAJDJn%2fzgPOSw34l4uI3Jqv0uv9WeTvN3zd33XkRA%3d -- 75ai13lvlmf6dw2prrr6um7fc6iw6l.oastify.com","4OuiZVN4gvvGUo64xOJbScdkjJSgOrBDkbdAFCcGuIE%3d -- ssssssssssssss.626liircfzpf1fdiunv48usokfq5eu.oastify.com","tBrNry0fMELkSE7mnh34Gnpn3sY8r2RRGnEzPl9i2UI%3d -- hpa14mqtaq6wkyw5jax1apqpigo6cv.oastify.com","BXjB%2b%2b37S%2fzWCSdk%2fEe4Zk87cEdIETINPhBYz10wmPY%3d -- bn2nkr237htuuf6p0zyclzbwvn1dp2.oastify.com","rhvjLAHu253uaZeM8%2bH0KDHldFXHXb5%2b76XREqPYD3M%3d -- aguer6td5weh36b1tluojp6ojfp5du.oastify.com","I%2b93dVfbyzh8%2bvmitC04DvhzNTnx%2f6bMiYePxJ3%2ffS0%3d -- wtt65xr474lsoekc3c9m2tj8xz3pre.oastify.com","PEFClwEt9fqGVw0obcxiFFlXQQcgKVpyCJUGgx%2bnAqw%3d -- rxsfedwptodug52vzc0x93oxdoje73.oastify.com","TFdLpaEUZXhzRGIJ0Crt6D6Xfd6PHx%2brO2AK8KkhPOg%3d -- bcrm6l0odpqdh63rrsgtvr0fn6twhl.oastify.com","FufIw7X4CgebdPObYX%2b66zhKU%2bVQX7itX7JmQ7oaduA%3d -- ybdobu30ovpcfgareuaqo5fufllb90.oastify.com","hUsB9spQu7%2bIeEp44bZ8VvJDeuavwNODjLLk4JF1ozw%3d -- 4x2drs9ozvbtnut4lxpqqnaujlpbd0.oastify.com","J%2bIXs7BO0vyeDeueXDC8X2V8X3j5pR5Kr9a04WxoC9g%3d -- f8ous137m2bofzppk19z8z0tvk1apz.oastify.com","dKZUaO1sNLzURSVROB6bgWB9h9CT5w3x049j4sLkEjo%3d -- pzs21x6wgorgne0o3b97ofwsejk98y.oastify.com","Zu7qe1u1tnHqLijlas4HgUaAQMQI13yUwx0NTV9wXKs%3d -- usqfclvxz74ozktghg5zzx3dm4sugj.oastify.com","QJNPHr19xvnDj6FvqybWl8C0sRTjbV%2bGp0O71BP%2bwKg%3d -- qztgzr1wgwfezuhy49my0dnydpjf74.oastify.com","QPazBq4DWhO%2bR1dHWBH6pCa9BuhYgvZ8wNbo5%2fHNAHg%3d -- a6hjjbdjl7zyfkojrf9w5hyp8ge62v.oastify.com","%2bHBKYr5rOACL1z5ln4cTFVGLGRhTfQDG0Zb8yy%2bYlHo%3d -- fratyn534jlquw3s499c74s0grmha6.oastify.com","8wPKII2dqy06TW%2bLA7xTlyTi4KyiMfMlvOxNa6OwDGI%3d -- aaorl7ewwe2zpyodk963h6utgkmaaz.oastify.com","5D0n%2fnwXMDvS4glVY9NymgTlTtWPLqCgSWDSiQbwfWE%3d -- 5kqbxvtze1n3am91ptzzgow0oruhi6.oastify.com","U01eT%2f%2fgkXlDvZYln7hB%2bf9gpMVoaFG5YUE6DtGOG0c%3d -- eq86ycn7g1u3zn4r6k2lurieh5nvbk.oastify.com","%2byBygZtvQmHSzT31Rn8ru4eUQd9wVNrxgVnoIwxgt%2fw%3d -- 4v0s6geid9khzqyquiix8gbh086yun.oastify.com","k2u2898KUfNsrrb82cJri7Om%2fK%2farlJeWb%2ft20vA4ks%3d -- oiaz6ghag8kp30l5iq02kxuan1trhg.oastify.com","s4J7%2bBnbxq6syfPpK5Icfv%2bvUdMRzS0Z87z2cRORF3M%3d -- 3osoyvw81u30wxpelyiug4665xbnzc.oastify.com","fxxDquTy6WE1QYv5QwSktRROoGs8DsiEV6wkbr9EsnY%3d -- 378so38pk73xet4lix7muah7nytohd.oastify.com","zIxmi1kpwmrPNEnWz6fstgeHFxyclgKnF1U4eh7yNUg%3d -- fcvqqy0kmqxzmrti707myixpsgy6mv.oastify.com","%2bfGiq87gbcK1847YvlraGNIoVurhy7q5NY4AZH6HCLM%3d -- b4gtxztv3o0nlwhjuflfiug64xanyc.oastify.com","nJoGJRUMIWhlWxVP85ASR%2fHo%2bKF2jEed5KGINO7EhQo%3d -- 3vz6s6ziq0fg8vrfpt3vftcpwg26qv.oastify.com","1G3kFphnV5vVTWhHuGz2FFlDWBKTSzEskc0AFW6adCM%3d -- n4see12n1vf2276lo0pon2z2mtsjg8.oastify.com","Tv0DvC2oe4qIm5YHKUtL78YZua7vTXrakb300y0N0dc%3d -- 8eny6s0ehvqknoxt56b70u1rdij87x.oastify.com","YzcISQjwgNNvHboVIcL%2fQKuzXo7u36zKDZkCL%2f3zLeU%3d -- owouaa2oikcqqbmsn1nerl479yfo3d.oastify.com","OhRIX5PLmzhBGkXgcNjuP5M4JCXoY558caT5gsm%2fyHA%3d -- aiwb9giw0e0ljsr5h085dftmmds3gs.oastify.com","OwdLjqYbhP3a%2bilpyQL%2fGhiCAEOykhe7DP8BAWgs%2fpA%3d -- bp4g0j0ezkwofg3bao6hyx4mldr3fs.oastify.com","wAIPsL7cKvUg3A2pbQgov6AKv82sQUUvL0mUqjYrnB4%3d -- gau35zzk9gdnx371gkmgiq5hd8jy7n.oastify.com","OIDUwM5eI49oqimXcD9WiuwOGttuw8QHxTEWhPTMC8E%3d -- y43cmuv8vic85bryvy3zeqmg076xum.oastify.com","SDUsxFWEB7BVBR6xucnLA0ofhNaA8040tqXtc3MjhHM%3d -- q5wxd42y00xxj45fbed4zpuvlmrcf1.oastify.com","tb5JIjO4UHDiLzG6vxHCnO6Bwn%2ftY78htrEwdTvzES4%3d -- 5kq712sfjowfmau1kzg3nmu5jwpmdb.oastify.com","F9RLAt9PGin0AslQDLZS6wKErlsPaeXH1xbwcMK25rU%3d -- 378f71ilju5j2pa7dylrcqtfw62wql.oastify.com","nw3I9C9ZW%2bbmdym9uIPAQ8MvSHKadHvUyjhpVd5eUxA%3d -- lf467ytck0qi6vtf2dz58p40trzhn6.oastify.com","TIXjqBDYPNOb42uyDEfMcJTnd8CerLJj1%2bloczH%2bGv8%3d -- 2ord9oj94zp6lz7wc8clvpk4jvplda.oastify.com","lFgoOPzGA1cUcWDAlfEn6MStjj8e3nTKzvSGR%2bEa18s%3d -- hxissl6hyx4jlv4vxgmn20jdu40uoj.oastify.com","7nQpiz1zVEBIq1SjkP1jrVUU%2fkzmWmGgdl2m0HvdaiM%3d -- 0rs2ovt2cop6ybx0is4qz29vnmtch1.oastify.com","N9tGl1J4JG5rV7S14FLa0x5gl%2b9mRWkbG%2fLTnV2ON6w%3d -- caq3y2hgyy5gegc4px7tetoosfy5mu.oastify.com","pjf%2fTIscr7vICbEwYyxLvHYA3LbC3YAcw1Z2NkSrl3g%3d -- 86cdnyxeaed67x8j6fxn1ofds4yumj.oastify.com","WWo%2fXjXzuKPrzCd%2fSngAFUR7lCIBVlsYeqoyij6Ok%2fw%3d -- 0hi5csfhn2li4f3y7hb3lke12s8iw7.oastify.com","x3DHuamoDxMop4LHsvXlkq1M2r2kfo7WvAYAYTPubU8%3d -- tqnd65zsg0ecpjjawcc9mofej5pvdk.oastify.com","aMqwMh12kE%2bXMiTF8Pe%2bcrSoeUVPFLzoL1aWCO6QI3s%3d -- ub9hlvm5un63polzm05pnwweo5uvik.oastify.com","D%2b1YyvkkChy5GCFX%2fiXsJKCB9E99i%2f7FlvJZ27KmJm8%3d -- s4x4rjkhfg9nfca93n36hbxkcbi16q.oastify.com","YLb1Sr9xb%2fOMRh6K9D%2fwW%2b0CdCdXKIDvqiQywJDeV6s%3d -- 5lrd2ye6ecz1x4pcxema436ee5kv8k.oastify.com","hyn%2bfuXiPg3knTLUky%2bhkx1DCWh%2bGmEhSI0ANWfjfZA%3d -- d7lgwtqrb1nuzs8xjxqynu09a0gq4f.oastify.com","op2T%2fiCV3GNCepi%2fQWT5eeCY1u1KVc6fXelNiQxEfUM%3d -- 020k3pifltekneo7d74oof30ur0ho6.oastify.com"};
//	    reader.close();
//		Random r = new Random();
//		System.out.println(list_random_biids[r.nextInt(list_random_biids.length)]);
		/*
		String html = "<body lang=PT-BR style='tab-interval:35.4pt'><h1>xxxx<br>ssssssss<h3>xxxxxxxxxx</body>";
        HtmlImageGenerator hig = new HtmlImageGenerator();
        hig.loadHtml(html);
        hig.saveAsImage(new File("C:\\Users\\truong.lam\\Desktop\\testa.png"));
        */
        
        //Document doc = Jsoup.parse("<?xml version=\"1.0\"?><!DOCTYPE root [<!ENTITY test SYSTEM 'file:///etc/passwd'>]><root>&test;</root>");
//		doc.outputSettings().prettyPrint(false);
		// System.out.println(doc);
		
		
		/*
		 * String aa = "sql ||| xxxxxxxxxxxxxxxxxx"; if (aa.contains("|| |") ){
		 * System.out.println(aa.split(" \\|\\|\\| ")[0]); }
		 * 
		 * System.out.println(aa.replaceAll("^(.* \\|\\|\\|) ", ""));
		 */
		
//		PayIntConnector payint = new PayIntConnector();
//		String file = payint.genPayIntPayload("D:/Techlab/Tools/payint-master/");
//		System.out.println(file);
		
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");  
//		LocalDateTime now = LocalDateTime.now();  
//		ZonedDateTime zonedUTC = now.atZone(ZoneId.of("+07"));
//		System.out.println("d"+dtf.format(zonedUTC).toString().replace(" ", "t"));
		System.out.println(System.currentTimeMillis());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis()-3600000*7);
		
		System.out.println(timestamp.toString().split("\\.")[0]);
	  }
}
